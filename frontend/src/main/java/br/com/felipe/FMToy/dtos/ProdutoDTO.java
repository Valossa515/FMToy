package br.com.felipe.FMToy.dtos;

import br.com.felipe.FMToy.entities.Produto;

public record ProdutoDTO(Long id, String nome, Double preco) {
	
	public ProdutoDTO(Produto obj)
	{
		this(obj.getId(), 
		obj.getNome(),
		obj.getPreco());
	}
}
