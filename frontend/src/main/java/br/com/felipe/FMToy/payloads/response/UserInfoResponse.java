package br.com.felipe.FMToy.payloads.response;

import java.util.List;

public record UserInfoResponse(Long id, String username, String email, List<String> roles) {
	
}
