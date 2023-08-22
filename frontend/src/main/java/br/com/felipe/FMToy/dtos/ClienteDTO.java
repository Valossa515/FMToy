package br.com.felipe.FMToy.dtos;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record ClienteDTO(Long id,
		@NotEmpty(message = "Preenchimento obrigatório!!!") @Email(message = "Email inválido!!!") String email,
		@NotEmpty(message = "Preenchimento obrigatório!!!") @Length(min = 5, max = 120, message = "O tamanho deve ser entre 5 e 120 caracteres!!!") String username,
		@NotEmpty(message = "Preenchimento obrigatório!!!") @Length(min = 5, max = 120, message = "O tamanho deve ser entre 5 e 120 caracteres!!!") String senha)
		implements Serializable {

}
