package br.com.felipe.FMToy.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OpenAPIConfig {

	@Value("${fmtoy.openapi.dev-url}")
	private String devUrl;

	@Value("${fmtoy.openapi.prod-url}")
	private String prodUrl;
	
	 @Bean
	 OpenAPI myOpenAPI() {
	        Server devServer = new Server();
	        devServer.setUrl(devUrl);
	        devServer.setDescription("Server URL in Development environment");

	        Server prodServer = new Server();
	        prodServer.setUrl(prodUrl);
	        prodServer.setDescription("Server URL in Production environment");

	        Contact contact = new Contact();
	        contact.setEmail("fe_mmo@hotmail.com");
	        contact.setName("Felipe Martins");
	        contact.setUrl("https://github.com/Valossa515");

	        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

	        Info info = new Info()
	                .title("API Loja de brinquedos virtual FMToy")
	                .version("1.0")
	                .contact(contact)
	                .description("Esta API expõe endpoints para demonstração.").termsOfService("https://github.com/Valossa515/FMToy/blob/main/README.md")
	                .license(mitLicense);

	        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
	    }
}
