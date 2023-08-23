package br.com.felipe.FMToy.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.felipe.FMToy.entities.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>{

	List<Categoria> findAllByIdIn(List<Long> ids);
	
}
