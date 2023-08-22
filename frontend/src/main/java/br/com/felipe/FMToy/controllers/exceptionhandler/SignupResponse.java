package br.com.felipe.FMToy.controllers.exceptionhandler;

import org.springframework.http.HttpStatus;

import br.com.felipe.FMToy.payloads.response.UserInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {
	private HttpStatus status;
    private String message;
    private UserInfoResponse userInfo;
}
