package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario crear(Usuario usuario);

    List<Usuario> listar();

    Usuario actualizar(Long id, Usuario usuario);
}
