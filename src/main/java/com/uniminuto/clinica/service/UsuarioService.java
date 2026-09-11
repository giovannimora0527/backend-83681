package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;
import java.util.List;

public interface UsuarioService {
    Usuario crearUsuario(Usuario usuario);

    Usuario actualizarUsuario(Usuario usuario);

    List<Usuario> getlistarUsuarios();

    List<Usuario> listarUsuarios();

    Usuario actualizarUsuario(Long id, Usuario usuario);
}