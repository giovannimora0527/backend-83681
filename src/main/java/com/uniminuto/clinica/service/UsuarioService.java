package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRS;

import java.util.List;

public interface UsuarioService {

    List<UsuarioRS> listarUsuarios();

    UsuarioRS crearUsuario(UsuarioRq usuarioRq)
            throws BadRequestException;

    UsuarioRS actualizarUsuario(UsuarioRq usuarioRq)
            throws BadRequestException;
}
