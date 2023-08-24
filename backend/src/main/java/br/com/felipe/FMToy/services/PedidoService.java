package br.com.felipe.FMToy.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.entities.ItemPedido;
import br.com.felipe.FMToy.entities.Pagamento;
import br.com.felipe.FMToy.entities.PagamentoComBoleto;
import br.com.felipe.FMToy.entities.PagamentoComCartao;
import br.com.felipe.FMToy.entities.Pedido;
import br.com.felipe.FMToy.entities.enums.EstadoPagamento;
import br.com.felipe.FMToy.kafka.KafkaProducerConfig;
import br.com.felipe.FMToy.repositories.BoletoService;
import br.com.felipe.FMToy.repositories.EnderecoRepository;
import br.com.felipe.FMToy.repositories.ItemPedidoRepository;
import br.com.felipe.FMToy.repositories.PagamentoRepository;
import br.com.felipe.FMToy.repositories.PedidoRepository;
import br.com.felipe.FMToy.security.UserDetailsImpl;
import br.com.felipe.FMToy.services.exceptions.AuthorizationException;
import br.com.felipe.FMToy.services.exceptions.ObjectNotFoundException;
import jakarta.validation.Valid;

@Service
public class PedidoService {
	
	@Autowired
	private PedidoRepository pedidoRepository;
	@Autowired
	private ProdutoService produtoService;
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	@Autowired
	private ClienteService clienteService;
	@Autowired
	private EmailService emailService;
	@Autowired
	PagamentoRepository pagamentoRepository;
	@Autowired
	BoletoService boletoService;
	@Autowired
	EnderecoRepository enderecoRepository;
	@Autowired
	KafkaProducerConfig kafkaProducerConfig;
	
	public Pedido find(Long id) {
		Optional<Pedido> obj = pedidoRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}
	
	@Transactional
	@Validated
	public Pedido insert(@Valid Pedido obj)
	{
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		validateTransactionData(obj.getPagamento());
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagementoComBoleto(pagto, obj.getInstante());
		}
		obj = pedidoRepository.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		emailService.sendOrderConfirmationHtmlEmail(obj);
		kafkaProducerConfig.sendMessage("Dados do pedido: " + "\n" + obj);
		return obj;
	}
	
	
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		UserDetailsImpl user = UserService.authenticated();
		if (user == null) {
			throw new AuthorizationException("Acesso negado");
		}
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		Cliente cliente =  clienteService.find(user.getId());
		return pedidoRepository.findByCliente(cliente, pageRequest);
	}
	
	public void validateTransactionData(Pagamento pagamento) {
		
		if (pagamento instanceof PagamentoComCartao) {
            PagamentoComCartao pagamentoComCartao = (PagamentoComCartao) pagamento;

            // Validation of the card number
            String numeroDoCartao = pagamentoComCartao.getNumeroDoCartao();
            if (numeroDoCartao == null || !numeroDoCartao.matches("^[0-9]{16}$")) {
            	kafkaProducerConfig.sendMessage("Número do cartão inválido" + numeroDoCartao);
                throw new IllegalArgumentException("Número do cartão inválido");
                
            }

            // Validation of CVV
            String cvv = pagamentoComCartao.getCvv();
            if (cvv == null || !cvv.matches("^[0-9]{3,4}$")) {
            	kafkaProducerConfig.sendMessage("Número do cvv inválido" + cvv);
                throw new IllegalArgumentException("CVV inválido");
            }
            
            if (!isValidCardDate(pagamentoComCartao.getDataValidade())) {
                throw new IllegalArgumentException("Data de validade do cartão inválida");
            }
        }
    }
	
	private boolean isValidCardDate(Date dataValidade) {
	    Date hoje = new Date();
	    return dataValidade != null && dataValidade.after(hoje);
	}

}
