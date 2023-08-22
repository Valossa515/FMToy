package br.com.felipe.FMToy.dtos;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record EmailDTO(
		@NotEmpty(message = "Preenchimento obrigatório!!!") @Email(message = "Email inválido!!!") String email)
		implements Serializable {
}
