package br.com.felipe.FMToy.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.felipe.FMToy.entities.Roles;
import br.com.felipe.FMToy.entities.enums.ERole;

public interface RoleRepository extends JpaRepository<Roles, Long>{
	Optional<Roles> findByName(ERole name);
}
