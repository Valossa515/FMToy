package br.com.felipe.FMToy.entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeName;

import br.com.felipe.FMToy.entities.enums.EstadoPagamento;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonTypeName("pagamentoComCartao")
public class PagamentoComCartao extends Pagamento{
	private static final long serialVersionUID = 1L;

	private Integer numeroDeParcelas;
	private String numeroDoCartao;
	private String cvv;
	@JsonFormat(pattern = "MM/yy")
	private Date dataValidade;
	
	public PagamentoComCartao(Long id, EstadoPagamento estado, Pedido pedido, Integer numeroDeParcelas,
			String numeroCartao, String cvv, Date date) throws ParseException {
		super(id, estado, pedido);
		this.numeroDeParcelas = numeroDeParcelas;
		this.numeroDoCartao = numeroCartao;
		this.cvv = cvv;
		this.dataValidade = date;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String formattedDate = sdf.format(date);
	    try {
	        this.dataValidade = sdf.parse(formattedDate);
	    } catch (ParseException e) {
	       throw new ParseException(formattedDate, 0);
	    }
		
	}
}
