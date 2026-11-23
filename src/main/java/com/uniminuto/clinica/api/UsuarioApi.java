package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
<<<<<<< HEAD
import com.uniminuto.clinica.models.PerfilRs;
=======
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Metodo que lista los usuariosRs organizados alfabeticamente.
     *
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
<<<<<<< HEAD
    @GetMapping(value = "/listar-ordenado",
            produces = {"application/json"})
    ResponseEntity<List<UsuarioRs>> getUsuarios()
=======
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Usuario>> listarUsuarios()
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            throws BadRequestException;

    @GetMapping(value = "/perfiles", produces = {"application/json"})
    ResponseEntity<List<PerfilRs>> getPerfiles()
            throws BadRequestException;


    /**
     * Metodo que guarda un nuevo usuario.
     *
     * @param usuarioRq Datos del usuario a guardar.
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
<<<<<<< HEAD
    ResponseEntity<MiRespuestaRS> guardar(
=======
    ResponseEntity<MiRespuestaRS> guardarUsuario(
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;

    /**
     * Metodo que actualiza un usuario existente.
     *
     * @param usuarioRq Datos del usuario a actualizar.
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
<<<<<<< HEAD
    ResponseEntity<MiRespuestaRS> actualizar(
=======
    ResponseEntity<MiRespuestaRS> actualizarUsuario(
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;
}
