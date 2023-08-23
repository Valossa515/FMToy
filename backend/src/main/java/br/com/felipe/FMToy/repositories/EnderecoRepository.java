package br.com.felipe.FMToy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.felipe.FMToy.entities.Endereco;

public interface EnderecoRepository extends JpaRepository<Endereco, Long>{
	
}
