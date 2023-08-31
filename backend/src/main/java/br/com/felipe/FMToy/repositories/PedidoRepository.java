package br.com.felipe.FMToy.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.entities.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long>{
	
	@Transactional(readOnly=true)
	Page<Pedido> findByCliente(Cliente cliente, Pageable pageRequest);
	List<Pedido> findByPagamentoEstado(Integer id);
}
