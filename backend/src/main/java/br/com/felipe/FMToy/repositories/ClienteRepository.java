package br.com.felipe.FMToy.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.felipe.FMToy.dtos.ClienteNewDTO;
import br.com.felipe.FMToy.entities.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long>{
	Optional<Cliente> findByUsername(String username);
	Boolean existsByUsername(String username);
	Boolean existsByEmail(String email);
	Cliente findByEmail(String email);
	Cliente findByEmail(ClienteNewDTO email);
}
