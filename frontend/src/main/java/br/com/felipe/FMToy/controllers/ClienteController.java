package br.com.felipe.FMToy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.felipe.FMToy.dtos.ClienteDTO;
import br.com.felipe.FMToy.dtos.ClienteNewDTO;
import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.services.ClienteService;
import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/clientes")
public class ClienteController {

	@Autowired
	private ClienteService clienteService;

	@GetMapping(value = "/{id}")
	public ResponseEntity<Cliente> find(@PathVariable Long id) {
		Cliente obj = clienteService.find(id);
		return ResponseEntity.ok().body(obj);
	}

	@GetMapping("/email")
	public ResponseEntity<Cliente> find(@RequestParam(value = "value") String email) {
		Cliente obj = clienteService.findByEmail(email);
		return ResponseEntity.ok().body(obj);
	}

	@PreAuthorize("hasAnyRole('ROLE_USER')")
	@PutMapping("/perfil/{id}")
	public ResponseEntity<Cliente> updateProfileAndAddress(@PathVariable Long id,
			@Valid @RequestBody ClienteNewDTO objDto) {
		Cliente obj = clienteService.updateProfileAndAddress(id, objDto);
		return ResponseEntity.ok(obj);
	}

	@PutMapping("/{id}/credenciais")
	public ResponseEntity<Cliente> updateCredentials(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO) {
		Cliente updatedCliente = clienteService.updateCredentials(id, clienteDTO);
		return ResponseEntity.ok(updatedCliente);
	}
}
