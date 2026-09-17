package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Lista los usuarios ordenados alfabeticamente de la A a la Z.
     * Nunca incluye el password, por seguridad.
     *
     * @return lista de usuarios.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<UsuarioRs>> listarUsuarios()
            throws BadRequestException;

    /**
     * Crea un usuario nuevo. El password se cifra en Hash MD5 antes de guardarlo.
     *
     * @param usuarioRq datos del usuario a crear.
     * @return usuario creado, sin password.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRs> crearUsuario(

            @RequestBody UsuarioRq usuarioRq)
            throws BadRequestException;

    /**
     * Actualiza un usuario existente. El password es opcional: Si no se relaciona,
     * se conserva el hash anterior; si se relaciona, se vuelve a cifrar en MD5..
     *
     * @param usuarioRq datos del usuario a actualizar (debe incluir usuarioId).
     * @return usuario actualizado, sin password.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<UsuarioRs> actualizarUsuario(
            @RequestBody UsuarioRq usuarioRq)
            throws BadRequestException;
}
