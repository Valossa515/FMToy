package br.com.felipe.FMToy.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class SmtpEmailService extends AbstractMailService{
	
	@Autowired
	private MailSender mailSender;
	@Autowired
	private JavaMailSender javaMailSender;
	
	private static final Logger LOG = LoggerFactory.getLogger(SmtpEmailService.class);
	
	@Override
	public void sendEmail(SimpleMailMessage msg) {
		LOG.info("Enviando...");
		mailSender.send(msg);
		LOG.info("Email enviado!!!");
		
	}
	@Override
	public void sendHtmlEmail(MimeMessage msg) {
		LOG.info("Enviando...");
		javaMailSender.send(msg);
		LOG.info("Email enviado!!!");
	}
	
}	
