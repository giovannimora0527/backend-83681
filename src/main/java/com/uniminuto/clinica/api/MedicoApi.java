package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CuentaMedicoRq;
import com.uniminuto.clinica.models.CuentaMedicoRs;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Expone el directorio veterinario y permite a Administración consultar y gestionar el vínculo
 * entre cada médico y su cuenta.
 */
@Tag(name = "Veterinarios y cuentas", description = "Administración gestiona médicos y vínculos de cuenta. Recepción puede consultar el directorio.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medico")
public interface MedicoApi {

    /** Consulta las cuentas que pueden asociarse a un veterinario. */
    @Operation(summary = "Listar cuentas veterinarias disponibles")
    @GetMapping("/cuentas-disponibles")
    List<CuentaMedicoRs> listarCuentasDisponibles();

    /** Consulta la cuenta de acceso asociada al veterinario. */
    @Operation(summary = "Consultar cuenta vinculada")
    @GetMapping("/{medicoId}/cuenta")
    ResponseEntity<CuentaMedicoRs> consultarCuenta(@PathVariable Long medicoId);

    /** Relaciona la cuenta seleccionada con el veterinario. */
    @Operation(summary = "Vincular cuenta con un veterinario")
    @PutMapping("/{medicoId}/cuenta")
    CuentaMedicoRs vincularCuenta(@PathVariable Long medicoId, @RequestBody CuentaMedicoRq solicitud);

    /** Desasocia la cuenta de acceso del veterinario indicado. */
    @Operation(summary = "Desvincular cuenta de un veterinario")
    @DeleteMapping("/{medicoId}/cuenta")
    ResponseEntity<Void> desvincularCuenta(@PathVariable Long medicoId);

    /** Consulta los veterinarios que pueden atender en el horario indicado. */
    @Operation(summary = "Consultar veterinarios disponibles en un intervalo")
    @GetMapping(value = "/listar-disponibles",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medico>> listarMedicosDisponibles(
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin,
            @RequestParam(required = false) Long citaId,
            @RequestParam(required = false) Long mascotaId)
            throws BadRequestException;


    /** Consulta todos los registros del catálogo. */
    @Operation(summary = "Listar veterinarios")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medico>> listarTodos()
            throws BadRequestException;

    /** Busca veterinarios que coincidan con el criterio. */
    @Operation(summary = "Buscar veterinarios")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medico>> buscarMedicos(@RequestParam String termino)
            throws BadRequestException;

    /** Registra un nuevo veterinario. */
    @Operation(summary = "Registrar veterinario")
    @PostMapping(value = "/crear",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearMedico(
            @RequestBody MedicoRq medicoRq
    )
            throws BadRequestException;

    /** Guarda los cambios del veterinario. */
    @Operation(summary = "Actualizar veterinario")
    @RequestMapping(value = "/actualizar", method = {RequestMethod.POST, RequestMethod.PUT},
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedico(
            @RequestBody MedicoRq medicoRq
    )
            throws BadRequestException;
}
