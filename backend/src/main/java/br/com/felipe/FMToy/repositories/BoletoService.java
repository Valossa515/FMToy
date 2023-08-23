package br.com.felipe.FMToy.repositories;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Service;

import br.com.felipe.FMToy.entities.PagamentoComBoleto;

@Service
public class BoletoService {

	public void preencherPagementoComBoleto(PagamentoComBoleto pagto, Date instanteDoPedido) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(instanteDoPedido);
		cal.add(Calendar.DAY_OF_MONTH, 7);
		pagto.setDataVencimento(cal.getTime());
	}
}
