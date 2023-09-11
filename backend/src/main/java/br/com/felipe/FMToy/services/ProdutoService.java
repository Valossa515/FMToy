package br.com.felipe.FMToy.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import br.com.felipe.FMToy.dtos.ProdutoDTO;
import br.com.felipe.FMToy.entities.Categoria;
import br.com.felipe.FMToy.entities.Produto;
import br.com.felipe.FMToy.repositories.CategoriaRepository;
import br.com.felipe.FMToy.repositories.ProdutoRepository;
import br.com.felipe.FMToy.services.exceptions.ObjectNotFoundException;

@Service
public class ProdutoService {
	@Autowired
	private ProdutoRepository produtoRepository;
	@Autowired
	private CategoriaRepository categoriaRepository;
	
	public Produto find(Long id) {
		Optional<Produto> obj = produtoRepository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Produto.class.getName()));
	}

	public Page<Produto> search(String nome, List<Long> idx, Integer page, Integer linesPerPage, String orderBy,
			String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		List<Categoria> categorias = categoriaRepository.findAllById(idx);
		return produtoRepository.findDistinctByNomeContainingAndCategoriasIn(nome, categorias, pageRequest);
	}
	
	public Produto insert(Produto produto, List<Long> categoriaIds) {
        List<Categoria> categorias = categoriaRepository.findAllByIdIn(categoriaIds);
        produto.setCategorias(categorias);
        return produtoRepository.save(produto);
    }
	
	public Produto update(Long id, Produto produto, List<Long> categoriaIds) {
        Produto novoProduto = find(id); // Encontra o produto pelo ID
        updateData(novoProduto, produto); // Atualiza os dados do produto
        List<Categoria> categorias = categoriaRepository.findAllByIdIn(categoriaIds);
        novoProduto.setCategorias(categorias); // Atualiza as categorias
        return produtoRepository.save(novoProduto); // Salva a atualização
    }
	
	public void delete(Long id) {
		Produto produto = find(id);
		
		produto.getCategorias().clear();
		produtoRepository.save(produto);
		produtoRepository.deleteById(id);
	}
	
	public void updateData(Produto novoProduto, Produto produto) {
        novoProduto.setNome(produto.getNome());
        novoProduto.setPreco(produto.getPreco());
    }

	
	public Produto fromDTO(ProdutoDTO produtoDTO) {
        Produto produto = new Produto();
        produto.setId(produtoDTO.id());
        produto.setNome(produtoDTO.nome());
        produto.setPreco(produtoDTO.preco());
        produto.setQuantidade(produtoDTO.quantidade());
        // Crie um set vazio para as categorias, elas serão associadas após a inserção
        produto.setCategorias(new ArrayList<>());
        return produto;
    }
}
