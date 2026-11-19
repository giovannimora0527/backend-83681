package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.InventarioLoteActualizacionRq;
import com.uniminuto.clinica.models.InventarioLoteRq;
import com.uniminuto.clinica.models.InventarioLoteRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

/**
 * API para la gestión de lotes de inventario.
 */
@Tag(name = "Lotes de inventario", description = "Administración y Despacho Médico pueden consultar. La escritura se reserva a Administración.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/inventario-lote")
public interface InventarioLoteApi {

    /** Lista lotes por fecha de vencimiento. */
    @Operation(summary = "Listar lotes")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<InventarioLoteRs>> listarLotes()
            throws BadRequestException;

    /** Lista los lotes asociados a un producto. */
    @Operation(summary = "Listar lotes de un medicamento")
    @GetMapping(value = "/listar-por-medicamento/{medicamentoId}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<InventarioLoteRs>> listarLotesPorMedicamento(
            @PathVariable("medicamentoId") Long medicamentoId)
            throws BadRequestException;

    /** Consulta un lote por su identificador. */
    @Operation(summary = "Consultar lote por ID")
    @GetMapping(value = "/buscar/{id}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<InventarioLoteRs> consultarLote(
            @PathVariable("id") Long id)
            throws BadRequestException;

    /** Registra una entrada por lote y el usuario responsable. */
    @Operation(summary = "Registrar lote y entrada de inventario")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearLote(
            @RequestBody InventarioLoteRq solicitud,
            @AuthenticationPrincipal String usuarioResponsableId)
            throws BadRequestException;

    /** Actualiza metadatos del lote sin modificar sus existencias. */
    @Operation(summary = "Actualizar datos de un lote")
    @PostMapping(value = "/actualizar/{id}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarLote(
            @PathVariable("id") Long id,
            @RequestBody InventarioLoteActualizacionRq solicitud)
            throws BadRequestException;

    /** Desactiva únicamente lotes sin saldo disponible. */
    @Operation(summary = "Desactivar un lote sin existencias")
    @PostMapping(value = "/desactivar/{id}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> desactivarLote(
            @PathVariable("id") Long id)
            throws BadRequestException;
}
