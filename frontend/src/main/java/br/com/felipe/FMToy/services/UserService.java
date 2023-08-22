package br.com.felipe.FMToy.services;

import org.springframework.security.core.context.SecurityContextHolder;

import br.com.felipe.FMToy.security.UserDetailsImpl;

public class UserService {
	public static UserDetailsImpl authenticated() {
		try {
			return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		catch (Exception e) {
			return null;
		}
	}
}
