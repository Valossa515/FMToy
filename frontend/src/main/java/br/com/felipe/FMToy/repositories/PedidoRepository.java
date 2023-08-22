package br.com.felipe.FMToy.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.felipe.FMToy.entities.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long>{
	
}
