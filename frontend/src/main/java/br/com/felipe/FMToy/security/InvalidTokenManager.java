package br.com.felipe.FMToy.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class InvalidTokenManager {
	private Set<String> invalidTokens = new HashSet<>();

	public void addInvalidToken(String token) {
		invalidTokens.add(token);
	}
	
	public boolean isTokenInvalid(String token) {
        return invalidTokens.contains(token);
    }
}
