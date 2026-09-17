package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;

import java.util.List;

public interface UsuarioService {


    List<UsuarioRs> listarUsuarios() throws BadRequestException;

    UsuarioRs crearUsuario(UsuarioRq usuarioRq) throws BadRequestException;

    UsuarioRs actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException;
}
