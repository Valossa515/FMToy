package br.com.felipe.FMToy.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.felipe.FMToy.dtos.ClienteDTO;
import br.com.felipe.FMToy.dtos.ClienteNewDTO;
import br.com.felipe.FMToy.entities.Cidade;
import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.entities.Endereco;
import br.com.felipe.FMToy.entities.enums.ERole;
import br.com.felipe.FMToy.repositories.ClienteRepository;
import br.com.felipe.FMToy.repositories.EnderecoRepository;
import br.com.felipe.FMToy.repositories.EstadoRepository;
import br.com.felipe.FMToy.security.UserDetailsImpl;
import br.com.felipe.FMToy.services.exceptions.AuthorizationException;
import br.com.felipe.FMToy.services.exceptions.DataIntegrityException;
import br.com.felipe.FMToy.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {
	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private BCryptPasswordEncoder pe;
	@Autowired
	CidadeService cidadeService;
	@Autowired
	EstadoRepository estadoRepository;

	public Cliente find(Long id) {
		UserDetailsImpl user = UserService.authenticated();

		if (user == null || !user.hasRole(ERole.ROLE_USER) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}
		Optional<Cliente> obj = clienteRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	@Transactional
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = clienteRepository.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;
	}

	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return clienteRepository.save(newObj);
	}

	public void delete(Long id) {
		find(id);
		try {
			clienteRepository.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há pedidos relacionadas");
		}
	}

	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	public Cliente findByEmail(String email) {
		UserDetailsImpl user = UserService.authenticated();
		if (user == null || !user.hasRole(ERole.ROLE_ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}

		Cliente obj = clienteRepository.findByEmail(email);
		if (obj == null) {
			throw new ObjectNotFoundException(
					"Objeto não encontrado! Id: " + user.getId() + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return clienteRepository.findAll(pageRequest);
	}

	public Cliente fromDTO(ClienteDTO objDto) {
		return new Cliente(objDto.id(), objDto.email(), objDto.username(), pe.encode(objDto.senha()));
	}

	public void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
		newObj.setSenha(obj.getSenha());
		newObj.setUsername(obj.getUsername());
		newObj.setEnderecos(obj.getEnderecos());
		newObj.setCpfOuCnpj(obj.getCpfOuCnpj());
		newObj.setTelefones(obj.getTelefones());
		newObj.setTipo(obj.getTipo());
	}

	@Transactional
	public Cliente updateProfileAndAddress(Long id, ClienteNewDTO objDto) {
		Cliente cliente = find(id);

		// Atualize os campos do perfil com base no objDto
		cliente.setNome(objDto.nome());
		cliente.setCpfOuCnpj(objDto.cpfOuCnpj());
		// ... atualize outros campos ...

		List<Endereco> enderecos = cliente.getEnderecos();
		Endereco enderecoToUpdate = enderecos.isEmpty() ? null : enderecos.get(0);

		if (enderecoToUpdate == null) {
			// Lide com o caso de não haver endereços no cliente
			enderecoToUpdate = new Endereco();
			// Preencha os campos do novo endereço com os valores do DTO
			// ...
			enderecoToUpdate.setLogradouro(objDto.logradouro());
			enderecoToUpdate.setNumero(objDto.numero());
			enderecoToUpdate.setComplemento(objDto.complemento());
			enderecoToUpdate.setBairro(objDto.bairro());
			enderecoToUpdate.setCep(objDto.cep());
			enderecoToUpdate.setCliente(cliente);
			cliente.getEnderecos().add(enderecoToUpdate);
		} else {
			// Atualize os campos do endereço com base no objDto
			enderecoToUpdate.setLogradouro(objDto.logradouro());
			enderecoToUpdate.setNumero(objDto.numero());
			enderecoToUpdate.setComplemento(objDto.complemento());
			enderecoToUpdate.setBairro(objDto.bairro());
			enderecoToUpdate.setCep(objDto.cep());
			// ... atualize outros campos ...
		}

		cliente.getTelefones().remove(objDto.telefone1()); // Remova o telefone1, caso já exista
		cliente.getTelefones().add(objDto.telefone1());

		if (enderecoToUpdate != null) {
			List<Cidade> cidades = cidadeService.findByEstado(objDto.estadoId());

			Cidade cidade = cidades.stream().filter(c -> c.getId().equals(objDto.cidadeId())).findFirst().orElse(null);

			if (cidade != null) {
				enderecoToUpdate.setCidade(cidade);
			}
		}

		cliente.setTipo(objDto.tipo());

		clienteRepository.save(cliente); // Salve o cliente com o endereço e estado atualizados

		return cliente;
	}

	@Transactional
	public Cliente updateCredentials(Long id, ClienteDTO objDTO) {
		Cliente cliente = find(id);
		if (!cliente.getEmail().equals(objDTO.email()) && clienteRepository.existsByEmail(objDTO.email())) {
			throw new RuntimeException("Email já está sendo usado por outro cliente");
		}
		if (!cliente.getUsername().equals(objDTO.username()) && clienteRepository.existsByUsername(objDTO.username())) {
			throw new RuntimeException("Username já está sendo usado por outro cliente");
		}
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String senhaEncriptada = encoder.encode(objDTO.senha());

		cliente.setEmail(objDTO.email());
		cliente.setUsername(objDTO.username());
		cliente.setSenha(senhaEncriptada);
		return clienteRepository.save(cliente);

	}

}
