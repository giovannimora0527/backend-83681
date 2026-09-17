package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.models.UsuarioRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Metodo para listar todos los usuarios.
     *
     *
     * @return Lista de usuarios.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Usuario>> listarUsuarios()
            throws BadRequestException;

    /**
     * Metodo para crear (guardar) un nuevo usuario.
     *
     * @return Respuesta de la operacion.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRS> guardarUsuario(
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;

    /**
     * Metodo para actualizar un usuario existente.
     *
     * @return Respuesta de la operacion.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRS> actualizarUsuario(
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;
}
