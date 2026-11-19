package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;

import java.util.List;
import java.util.Optional;

/** Define las operaciones disponibles para gestionar una cuenta de usuario. */
public interface UsuarioService {

    /** Consulta las cuentas en el orden definido para el listado. */
    List<UsuarioRs> obtenerUsuariosOrdenados();

    /** Busca los usuarios. */
    List<UsuarioRs> buscarUsuarios(String termino);

    /** Registra el usuario. */
    MiRespuestaRS crearUsuario(UsuarioRq usuarioRq);

    /** Actualiza el usuario. */
    MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq);
}
