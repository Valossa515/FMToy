package br.com.felipe.FMToy.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentScheduler {
	@Autowired
    private PedidoService pedidoService;
	
	@Scheduled(fixedDelay = 300000) // Agendamento a cada 5 minutos
    public void runPaymentChecker() {
       pedidoService.paymentChecker();
    }
}
