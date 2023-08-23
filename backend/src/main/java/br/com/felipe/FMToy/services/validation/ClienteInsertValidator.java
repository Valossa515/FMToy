package br.com.felipe.FMToy.services.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import br.com.felipe.FMToy.controllers.exceptionhandler.FieldMessage;
import br.com.felipe.FMToy.dtos.ClienteNewDTO;
import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.entities.enums.TipoCliente;
import br.com.felipe.FMToy.repositories.ClienteRepository;
import br.com.felipe.FMToy.services.validation.utils.BR;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ClienteInsertValidator implements ConstraintValidator<ClienteInsert, ClienteNewDTO>{
	
	@Autowired
	private ClienteRepository clienteReporitory;
	
	@Override
	public void initialize(ClienteInsert ann) {

	}
	
	@Override
	public boolean isValid(ClienteNewDTO objDto, ConstraintValidatorContext context) {
		List<FieldMessage> list = new ArrayList<>();
		Cliente cli = new Cliente();
		if(objDto.tipo().equals(TipoCliente.PESSOAFISICA.getCod()) && !BR.isValidCPF(objDto.cpfOuCnpj()))
		{
		  list.add(new FieldMessage("cpfOuCnpj", "CPF inválido"));
		}
		if(objDto.tipo().equals(TipoCliente.PESSOAJURIDICA.getCod()) && !BR.isValidCNPJ(objDto.cpfOuCnpj()))
		{
		  list.add(new FieldMessage("cpfOuCnpj", "CNPJ inválido"));
		}
		
		Cliente aux = clienteReporitory.findByEmail(cli.getEmail());
		
		if(aux != null)
		{
			list.add(new FieldMessage("email", "Email já existente!!!"));
		}
		
		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}
	
}
