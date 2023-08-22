package br.com.felipe.FMToy.dtos;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

import br.com.felipe.FMToy.entities.Categoria;
import jakarta.validation.constraints.NotNull;

public record CategoriaDTO(Long id,
		@NotNull(message = "Preenchimento obrigatório!")
        @Length(min = 5, max = 80, message = "O tamanho deve ser entre 5 e 80 caracteres!!!")
		String nome) implements Serializable {

	public CategoriaDTO(Categoria obj) {
		this(obj.getId(), obj.getNome());
	}
}
