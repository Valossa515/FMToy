package br.com.felipe.FMToy.entities;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.com.felipe.FMToy.entities.enums.ERole;
import br.com.felipe.FMToy.entities.enums.TipoCliente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Cliente implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nome;
	@Column(unique = true)
	private String email;
	private String cpfOuCnpj;
	@JsonIgnore
	private String senha;
	private String username;
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "cliente_roles", joinColumns = @JoinColumn(name = "cliente_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Roles> roles = new HashSet<>();
	@OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
	private List<Endereco> enderecos = new ArrayList<>();
	@ElementCollection
	@CollectionTable(name = "TELEFONE")
	private Set<String> telefones = new HashSet<>();
	private Integer tipo;
	@OneToMany(mappedBy = "cliente")
	@JsonIgnore
	private List<Pedido> pedidos = new ArrayList<>();
	
	public Cliente(Long id, String username, String email, String senha) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.senha = senha;
	}
	
	public Cliente(String username, String email, String senha) {
		this.username = username;
		this.email = email;
		this.senha = senha;
	}

	public Cliente(TipoCliente tipo) {
		this.tipo = (tipo == null) ? null : tipo.getCod();
	}

	
	
	public void addPerfil(ERole p)
	{
		roles.add(new Roles(null, p));
	}

}
