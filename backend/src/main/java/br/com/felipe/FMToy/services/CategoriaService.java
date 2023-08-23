package br.com.felipe.FMToy.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.felipe.FMToy.dtos.CategoriaDTO;
import br.com.felipe.FMToy.entities.Categoria;
import br.com.felipe.FMToy.repositories.CategoriaRepository;
import br.com.felipe.FMToy.services.exceptions.DataIntegrityException;
import br.com.felipe.FMToy.services.exceptions.ObjectNotFoundException;

@Service
public class CategoriaService {
	@Autowired
	private CategoriaRepository categoriaRepository;

	@Transactional
	public Categoria find(Long id) {
		Optional<Categoria> obj = categoriaRepository.findById(id);
		Categoria categoria = obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Categoria.class.getName()));
		// Forçar o carregamento da coleção de produtos dentro do contexto transacional
		categoria.getProdutos().size();
		return categoria;
	}

	@Transactional
	public Categoria insert(Categoria obj) {
		obj.setId(null);
		return categoriaRepository.save(obj);
	}

	@Transactional
	public Categoria update(Categoria obj) {
		Categoria newObj = find(obj.getId());
		updateData(newObj, obj);
		return categoriaRepository.save(newObj);
	}

	@Transactional
	public void delete(Long id) {
		find(id);

		try {
			categoriaRepository.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir uma cateogria que possui produtos!");
		}
	}

	@Transactional
	public List<Categoria> findAll() {
		return categoriaRepository.findAll();
	}

	@Transactional
	public Page<Categoria> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return categoriaRepository.findAll(pageRequest);
	}

	public Categoria fromDTO(CategoriaDTO objDTO) {
		return new Categoria(objDTO.id(), objDTO.nome());
	}

	public void updateData(Categoria newObj, Categoria obj) {
		newObj.setNome(obj.getNome());
	}
}
