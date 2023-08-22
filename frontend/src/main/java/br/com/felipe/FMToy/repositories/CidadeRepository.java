package br.com.felipe.FMToy.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import br.com.felipe.FMToy.entities.Cidade;

public interface CidadeRepository extends JpaRepository<Cidade, Long>{
	@Transactional(readOnly=true)
	@Query("SELECT obj FROM Cidade obj WHERE obj.estado.id = :estadoId ORDER BY obj.nome")
	List<Cidade> findCidades(@Param("estadoId") Long estado_id);
	Optional<Cidade> findById(Long id);
}
