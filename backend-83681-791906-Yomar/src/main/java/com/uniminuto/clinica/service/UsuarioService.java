package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;

import java.util.List;

public interface UsuarioService {

    List<Usuario> listarUsuarios();

    MiRespuestaRS guardarUsuario(UsuarioRq usuarioRq) throws BadRequestException;

    MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException;
}