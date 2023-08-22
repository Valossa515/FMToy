package br.com.felipe.FMToy.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.entities.Roles;
import br.com.felipe.FMToy.entities.enums.ERole;
import br.com.felipe.FMToy.kafka.KafkaProducerConfig;
import br.com.felipe.FMToy.payloads.request.LoginRequest;
import br.com.felipe.FMToy.payloads.request.SignupRequest;
import br.com.felipe.FMToy.payloads.response.JwtResponse;
import br.com.felipe.FMToy.payloads.response.MessageResponse;
import br.com.felipe.FMToy.repositories.ClienteRepository;
import br.com.felipe.FMToy.repositories.RoleRepository;
import br.com.felipe.FMToy.security.JwtUtils;
import br.com.felipe.FMToy.security.UserDetailsImpl;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	ClienteRepository clienteRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	private KafkaProducerConfig kafkaProducerConfig;

	@PostMapping("/signin")
	 public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

	    Authentication authentication = authenticationManager
	        .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
	    
	    SecurityContextHolder.getContext().setAuthentication(authentication);
	    String jwt = jwtUtils.generateJwtToken(authentication);

	    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	    List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
	        .collect(Collectors.toList());
	    String message = "Usuário logado com sucesso: "
	            + "ID: " + userDetails.getId()
	            + ", Username: " + userDetails.getUsername()
	            + ", Email: " + userDetails.getEmail()
	            + ", Roles: " + roles.toString();
	    kafkaProducerConfig.sendMessage(message);
	    return ResponseEntity
	        .ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
	  }


	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (clienteRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Erro: Este Username já existe!"));
		}

		if (clienteRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Erro: Este Email já existe!"));
		}

		// Create new user's account
		Cliente user = new Cliente(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Roles> roles = new HashSet<>();

		if (strRoles == null) {
			Roles userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Erro: Perfil não encontrado."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Roles adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Erro: Perfil não encontrado."));
					roles.add(adminRole);
					break;
				case "mod":
					Roles modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Erro: Perfil não encontrado."));
					roles.add(modRole);
					break;
				default:
					Roles userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Erro: Perfil não encontrado."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		clienteRepository.save(user);
		kafkaProducerConfig.sendMessage("Usuário registrado com sucesso: " + signUpRequest.getUsername());
		 return ResponseEntity.ok(new MessageResponse("Usuário registrado com sucesso!"));
	}
	
	@PostMapping("/signout")
    public ResponseEntity<String> signOut(@RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        // Invalidar o token
        jwtUtils.invalidateToken(token);
        return ResponseEntity.ok("Deslogado com sucesso.");
    }
}
