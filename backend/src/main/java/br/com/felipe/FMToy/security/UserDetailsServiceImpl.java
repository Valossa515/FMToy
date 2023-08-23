package br.com.felipe.FMToy.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.felipe.FMToy.entities.Cliente;
import br.com.felipe.FMToy.repositories.ClienteRepository;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	ClienteRepository userRepository;
	
	 @Override
	  @Transactional
	  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    Cliente user = userRepository.findByUsername(username)
	        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o username: " + username));
	    return UserDetailsImpl.build(user);
	  }

}
