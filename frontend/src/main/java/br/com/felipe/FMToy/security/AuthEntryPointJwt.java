package br.com.felipe.FMToy.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.felipe.FMToy.kafka.KafkaProducerConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
	@Autowired
	private KafkaProducerConfig kafkaProducerConfig;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		logger.error("Não autorizado, erro: {}", authException.getMessage());

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		final Map<String, Object> body = new HashMap<>();
		body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		body.put("error", "Não autorizado");
		if (authException instanceof UsernameNotFoundException) {
			body.put("message", "Usuário não encontrado");
		} else if (authException instanceof BadCredentialsException) {
			body.put("message", "Credenciais inválidas");
		} else {
			body.put("message", "Falha na autenticação");
		}
		body.put("path", request.getServletPath());
		final ObjectMapper mapper = new ObjectMapper();
		String errorMessage = mapper.writeValueAsString(body);
		kafkaProducerConfig.sendMessage(errorMessage);
		mapper.writeValue(response.getOutputStream(), body);
		
	}
}
