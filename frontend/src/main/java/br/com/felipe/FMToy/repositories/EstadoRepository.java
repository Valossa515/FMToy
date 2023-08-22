package br.com.felipe.FMToy.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import br.com.felipe.FMToy.entities.Estado;

public interface EstadoRepository extends JpaRepository<Estado, Long>{
	@Transactional(readOnly = true)
	List<Estado> findAllByOrderByNome();
}	
