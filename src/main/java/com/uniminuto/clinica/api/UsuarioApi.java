package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios y perfiles", description = "Administración. Gestión de cuentas de acceso y perfiles.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/usuario")
public interface UsuarioApi {

    /**
     * Metodo que lista los usuariosRs organizados alfabeticamente.
     *
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Listar usuarios")
    @GetMapping(value = {"/listar", "/listar-ordenado"},
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<UsuarioRs>> getUsuarios()
            throws BadRequestException;

    @Operation(summary = "Buscar usuarios")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<UsuarioRs>> buscarUsuarios(@RequestParam String termino)
            throws BadRequestException;


    /**
     * Metodo que guarda un nuevo usuario.
     *
     * @param usuarioRq Datos del usuario a guardar.
     * @return Lista<UsuarioRs> </>.
     * @throws BadRequestException excepcion.
     */
    @Operation(summary = "Registrar usuario")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardar(
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
    @Operation(summary = "Actualizar usuario")
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizar(
            @RequestBody UsuarioRq usuarioRq
    )
            throws BadRequestException;
}
