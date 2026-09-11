package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.models.UsuarioRq;

import java.util.List;

public interface UsuarioService {
    List<Usuario> listarUsuarios() throws BadRequestException;

    UsuarioRS guardarUsuario(UsuarioRq usuarioRq) throws BadRequestException;

    UsuarioRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException;
}
