package br.com.felipe.FMToy.dtos;

import java.io.Serializable;

import br.com.felipe.FMToy.entities.Estado;

public record EstadoDTO(Long id, String nome) implements Serializable{
	
	
	public EstadoDTO(Estado obj)
	{
		this(obj.getId(), obj.getNome());
	}
}
