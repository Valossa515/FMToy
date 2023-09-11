package br.com.felipe.FMToy.dtos;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

import br.com.felipe.FMToy.entities.Cidade;
import jakarta.validation.constraints.NotNull;

public record CidadeDTO(Long id,
		@NotNull(message = "Preenchimento obrigatório!") @Length(min = 5, max = 200, message = "O tamanho deve ser entre 5 e 200 caracteres!!!") String nome) implements Serializable{

	public CidadeDTO(Cidade obj) {
		this(obj.getId(), obj.getNome());
	}

}
