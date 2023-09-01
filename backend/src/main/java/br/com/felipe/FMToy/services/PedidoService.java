package br.com.felipe.FMToy.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.entities.ItemPedido;
import br.com.felipe.FMToy.entities.Pagamento;
import br.com.felipe.FMToy.entities.PagamentoComBoleto;
import br.com.felipe.FMToy.entities.PagamentoComCartao;
import br.com.felipe.FMToy.entities.Pedido;
import br.com.felipe.FMToy.entities.Produto;
import br.com.felipe.FMToy.entities.enums.EstadoPagamento;
import br.com.felipe.FMToy.kafka.KafkaProducerConfig;
import br.com.felipe.FMToy.repositories.EnderecoRepository;
import br.com.felipe.FMToy.repositories.ItemPedidoRepository;
import br.com.felipe.FMToy.repositories.PagamentoRepository;
import br.com.felipe.FMToy.repositories.PedidoRepository;
import br.com.felipe.FMToy.repositories.ProdutoRepository;
import br.com.felipe.FMToy.security.UserDetailsImpl;
import br.com.felipe.FMToy.services.exceptions.AuthorizationException;
import br.com.felipe.FMToy.services.exceptions.DataIntegrityException;
import br.com.felipe.FMToy.services.exceptions.InsufficientStockException;
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
	EnderecoRepository enderecoRepository;
	@Autowired
	KafkaProducerConfig kafkaProducerConfig;
	@Autowired
	BoletoService boletoService;
	@Autowired
	ProdutoRepository produtoRepository;
	private Set<Long> ordersWithSentEmails = new HashSet<>();

	public Pedido find(Long id) {
		Optional<Pedido> obj = pedidoRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}

	@Transactional
	@Validated
	public Pedido insert(@Valid Pedido obj) throws IOException {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		validateTransactionData(obj.getPagamento());

		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagementoComBoleto(pagto, obj.getInstante());
			gerarPdfBoleto(obj);
		}
		obj = pedidoRepository.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for (ItemPedido ip : obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		for (ItemPedido ip : obj.getItens()) {
			Produto produto = ip.getProduto();
			int quantidadePedido = ip.getQuantidade();

			if (quantidadePedido > produto.getQuantidade()) {
				throw new InsufficientStockException(
						"Quantidade em estoque insuficiente para o produto: " + produto.getNome());
			}
			produto.setQuantidade(produto.getQuantidade() - quantidadePedido);
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
		Cliente cliente = clienteService.find(user.getId());
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

	public byte[] gerarPdfBoleto(Pedido obj) {
		ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();

		Document document = new Document();
		double totalGeral = 0.0;
		try {
			PdfWriter.getInstance(document, pdfOutputStream);
			document.open();

			// Crie uma fonte para o título
			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);

			// Adicione informações do pedido

			Paragraph titlePedido = new Paragraph("Detalhes do Pedido", titleFont);
			Paragraph infoPedido = new Paragraph(
					"Número do Pedido: " + obj.getId() + "\n" + "Data do Pedido: " + obj.getInstante()
			// Outras informações do pedido
			);

			// Adicione informações da empresa
			Paragraph titleEmpresa = new Paragraph("Dados da Empresa", titleFont);
			Paragraph infoEmpresa = new Paragraph("Nome da Empresa: Empresa XYZ\n" + "Endereço: Rua ABC, 123\n"
			// Outras informações da empresa
			);

			// Crie um código de barras para o boleto
			Code128Bean code128Bean = new Code128Bean();
			final int dpi = 150;
			String barcodeContent = "ORDER-" + obj.getId() + obj.getInstante().getTime();
			BitmapCanvasProvider canvas = new BitmapCanvasProvider(pdfOutputStream, "image/png", dpi,
					BufferedImage.TYPE_BYTE_BINARY, false, 0);
			code128Bean.generateBarcode(canvas, barcodeContent);
			canvas.finish();
			// Adicione os parágrafos ao documento
			document.add(titlePedido);
			document.add(infoPedido);
			document.add(titleEmpresa);
			document.add(infoEmpresa);

			Paragraph titleItens = new Paragraph("Itens do Pedido", titleFont);
			document.add(titleItens);

			for (ItemPedido ip : obj.getItens()) {
				ip.setDesconto(0.0);
				ip.setProduto(produtoService.find(ip.getProduto().getId()));
				ip.setPreco(ip.getProduto().getPreco());
				ip.setPedido(obj);

				double subtotal = ip.getSubTotal();
				// Adicione informações do item ao documento, como descrição, quantidade, preço,
				// subtotal, etc.
				Paragraph infoItem = new Paragraph(
						"Descrição: " + ip.getProduto().getNome() + "\n" + "Quantidade: " + ip.getQuantidade() + "\n"
								+ "Preço Unitário: " + ip.getPreco() + "\n" + "Subtotal: " + subtotal
				// Calcule e adicione o subtotal aqui
				);

				document.add(infoItem);
				totalGeral += subtotal;

			}
			// Adicione o total geral ao documento
			Paragraph totalGeralParaPDF = new Paragraph("Total Geral: " + totalGeral);
			document.add(totalGeralParaPDF);
			Image barcodeImage = Image.getInstance(pdfOutputStream.toByteArray());
			document.add(barcodeImage);

			document.close();
		} catch (DocumentException e) {
			// Lidar com exceções (pode lançar exceção personalizada, log, etc.)
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pdfOutputStream.toByteArray();
	}

	public void cancelarPedido(Long id, Date dataAtual) {
		Pedido pedido = find(id);
		Date dataCompra = pedido.getInstante();
		long diferencaMilissegundos = dataAtual.getTime() - dataCompra.getTime();
		long diferencaDias = diferencaMilissegundos / (24 * 60 * 60 * 1000);
		if (diferencaDias < 7) {
			// Primeiro, atualize as quantidades em estoque dos produtos associados
			for (ItemPedido ip : pedido.getItens()) {
				Produto produto = ip.getProduto();
				int quantidadePedido = ip.getQuantidade();
				produto.setQuantidade(produto.getQuantidade() + quantidadePedido);
				produtoRepository.save(produto); // Salve as alterações no produto
			}
			// Em seguida, remova os itens do pedido da tabela "item_pedido"
			pedido.getItens().clear();
			pedido.getPagamento().setEstado(EstadoPagamento.CANCELADO);
			pedidoRepository.save(pedido); // Salve o pedido modificado
			
			// Finalmente, exclua o pedido
			try {
				pedidoRepository.deleteById(id);
				emailService.sendCancelConfirmationEmail(pedido);
			} catch (DataIntegrityViolationException e) {
				throw new DataIntegrityException("Não é possível excluir!");
			}
		} else {
			throw new IllegalStateException("Não é possível cancelar o pedido depois de 7 dias da data da compra.");
		}
	}

	@Transactional
	public void paymentChecker() {
		List<Pedido> pedidosPendentes = findPedidosPendentesDePagamento();
		for (Pedido p : pedidosPendentes) {
			Pagamento pagamento = p.getPagamento();
			if (pagamento.getEstado().equals(EstadoPagamento.PENDENTE)) {
				if (!ordersWithSentEmails.contains(p.getId())) {
					pagamento.setEstado(EstadoPagamento.QUITADO);
					pagamentoRepository.save(pagamento);
					emailService.sendPaymentConfirmationEmail(p);
					ordersWithSentEmails.add(p.getId());
				}
			} else {
				ordersWithSentEmails.remove(p.getId());
			}
		}
		kafkaProducerConfig.sendMessage("Pagamento aprovado!");
	}

	public List<Pedido> findPedidosPendentesDePagamento() {
		return pedidoRepository.findByPagamentoEstado(EstadoPagamento.PENDENTE.getCod());
	}
}
