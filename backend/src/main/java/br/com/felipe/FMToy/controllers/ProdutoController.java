package br.com.felipe.FMToy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.felipe.FMToy.controllers.utils.URL;
import br.com.felipe.FMToy.dtos.ProdutoDTO;
import br.com.felipe.FMToy.entities.Produto;
import br.com.felipe.FMToy.services.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/produtos")
@Tag(name = "Enpoint Produto",
description = "Tutorial de como funciona o Endpoint de produtos")
public class ProdutoController {

	@Autowired
	private ProdutoService produtoService;
	
	@Operation(
		      summary = "Recupera um Produto por Id",
		      description = "Obtem um objeto de Produto pelo seu id. A repsota é um objeto Produto com id, nome e preço.",
		      tags = { "produtos", "get" })
		  @ApiResponses({
		      @ApiResponse(responseCode = "200", content = { @Content(schema = @Schema(implementation = Produto.class), mediaType = "application/json") }),
		      @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
		      @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
	@GetMapping("/{id}")
	public ResponseEntity<Produto> find(@PathVariable Long id) {
		Produto obj = produtoService.find(id);
		return ResponseEntity.ok().body(obj);
	}

	@GetMapping("/pages")
	public ResponseEntity<Page<ProdutoDTO>> findPage(@RequestParam(value = "nome", defaultValue = "") String nome,
			@RequestParam(value = "categorias", defaultValue = "") String categorias,
			@RequestParam(value = "page", defaultValue = "0") Integer page,
			@RequestParam(value = "linesPerPage", defaultValue = "24") Integer linesPerPage,
			@RequestParam(value = "orderBy", defaultValue = "nome") String orderBy,
			@RequestParam(value = "direction", defaultValue = "ASC") String direction) {
		List<Long> ids = URL.decodeLongList(categorias);
		String nomeDecoded = URL.decodeParam(nome);
		Page<Produto> list = produtoService.search(nomeDecoded, ids, page, linesPerPage, orderBy, direction);

		// "Page" já é Java 8 compliance, então não é necessário o uso do stream e do
		// collect
		Page<ProdutoDTO> listDto = list.map(obj -> new ProdutoDTO(obj));
		return ResponseEntity.ok().body(listDto);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<Produto> insert(@RequestBody ProdutoDTO produtoDTO, @RequestParam List<Long> categoriaIds) {
		Produto novoProduto = produtoService.fromDTO(produtoDTO);
		novoProduto = produtoService.insert(novoProduto, categoriaIds);
		return new ResponseEntity<>(novoProduto, HttpStatus.CREATED);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<Produto> update(@PathVariable Long id, @RequestBody Produto produto,
			@RequestParam List<Long> categoriaIds) {
		Produto produtoAtualizado = produtoService.update(id, produto, categoriaIds);

		return ResponseEntity.ok(produtoAtualizado);
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	@DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
