package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/usuario")

public interface UsuarioApi {

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Usuario>> getUsuario()
            throws BadRequestException;

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarUsuario(
            @RequestBody UsuarioRq usuarioRqRq
    )
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarUsuario(
            @RequestBody UsuarioRq usuarioRqRq
    )
            throws BadRequestException;
}
