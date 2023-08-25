package br.com.felipe.FMToy.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.entities.Pedido;
import jakarta.mail.internet.MimeMessage;

@Service
public interface EmailService {
	void sendOrderConfirmationEmail(Pedido obj);
	void sendEmail(SimpleMailMessage msg);
	void sendOrderConfirmationHtmlEmail(Pedido obj);
	void sendHtmlEmail(MimeMessage msg);
	void sendNewPasswordEmail(Cliente cliente, String newPass);
	
}
