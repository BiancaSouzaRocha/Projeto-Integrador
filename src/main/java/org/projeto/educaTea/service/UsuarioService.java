package org.projeto.educaTea.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.projeto.educaTea.model.UserLogin;
import org.projeto.educaTea.model.Usuario;
import org.projeto.educaTea.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository repository;

	public Usuario CadastrarUsuario(Usuario usuario) {

		Optional<Usuario> optional = repository.findByNomeUsuario(usuario.getNomeUsuario());

		if (optional.isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Usuario ja existente, cadastre com outro email!");
		} else {

			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

			String senhaEncoder = encoder.encode(usuario.getSenha());
			usuario.setSenha(senhaEncoder);

			return repository.save(usuario);
		}
	}

	public Optional<UserLogin> Logar(Optional<UserLogin> user) {

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Optional<Usuario> usuario = repository.findByNomeUsuario(user.get().getNomeUsuario());

		if (usuario.isPresent()) {
			if (encoder.matches(user.get().getSenha(), usuario.get().getSenha())) {

				String auth = user.get().getNomeUsuario() + ":" + user.get().getSenha();
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);

				user.get().setToken(authHeader);
				user.get().setNomeUsuario(usuario.get().getNomeUsuario());
				user.get().setSenha(usuario.get().getSenha());
				user.get().setFoto(usuario.get().getFoto());

				return user;
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha ou usuário inválidos!");
			}
		}

		return null;
	}

	public ResponseEntity<Usuario> atualizarUsuario(Usuario usuario) {
		if (repository.findById(usuario.getId()).isPresent()) {
			Optional<Usuario> buscaUsuario = repository.findByNomeUsuario(usuario.getNomeUsuario());
			if (buscaUsuario.isPresent()) {
				if (buscaUsuario.get().getId() != usuario.getId())
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);
			}
			usuario.setSenha(criptografarSenha(usuario.getSenha()));
			return ResponseEntity.status(200).body(repository.save(usuario));
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!", null);
	}

		private String criptografarSenha(String senha) {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String senhaEncoder = encoder.encode(senha);
			return senhaEncoder;
	}

}
