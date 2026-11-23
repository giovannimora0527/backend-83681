package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;
<<<<<<< HEAD
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PerfilRs;
=======
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

<<<<<<< HEAD
    List<UsuarioRs> obtenerUsuariosOrdenados();

    List<PerfilRs> obtenerPerfilesActivos();

    MiRespuestaRS crearUsuario(UsuarioRq usuarioRq);

    MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq);
=======
    MiRespuestaRS guardarUsuario(UsuarioRq usuarioRq) throws BadRequestException;

    MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
