package br.com.felipe.FMToy.dtos;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;

public record ClienteNewDTO(
		@NotEmpty(message = "Preenchimento obrigatório!!!")
        @Length(min = 5, max = 120, message = "O tamanho deve ser entre 5 e 120 caracteres!!!") String nome,
        @NotEmpty(message = "Preenchimento obrigatório!!!") String cpfOuCnpj, 
        Integer tipo,
        @NotEmpty(message = "Preenchimento obrigatório!!!") String logradouro,
        @NotEmpty(message = "Preenchimento obrigatório!!!") String numero, 
        String complemento, String bairro,
        @NotEmpty(message = "Preenchimento obrigatório!!!") String telefone1, 
        String telefone2, String telefone3,
        Long cidadeId, 
        String cep, 
        Long estadoId) implements Serializable {
}
