package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Expone la consulta, creación y edición de especialidades. La edición permite activarlas o
 * inactivarlas.
 */
@Tag(name = "Especialidades", description = "Administración. Catálogo de especialidades veterinarias.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/especializacion")
public interface EspecializacionApi {

    @Operation(summary = "Listar especialidades")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Especializacion>> listarEspecializaciones()
            throws BadRequestException;

    @Operation(summary = "Buscar especialidades")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Especializacion>> buscarEspecializaciones(@RequestParam String termino)
            throws BadRequestException;


    @Operation(summary = "Registrar especialidad")
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearEspecializacion(
            @RequestBody EspecializacionRq especializacion
    )
            throws BadRequestException;


    @Operation(summary = "Actualizar o cambiar el estado de una especialidad")
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarEspecializacion(
            @RequestBody EspecializacionRq especializacion
    )
            throws BadRequestException;
}
