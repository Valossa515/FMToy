package br.com.felipe.FMToy.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.felipe.FMToy.entities.Cidade;
import br.com.felipe.FMToy.repositories.CidadeRepository;

@Service
public class CidadeService {
	@Autowired
	private CidadeRepository cidadeRepository;
	
	public List<Cidade> findByEstado(Long estadoId) {
		return cidadeRepository.findCidades(estadoId);
	}
}
