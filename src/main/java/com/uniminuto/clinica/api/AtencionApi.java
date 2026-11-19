package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.models.AtencionDetalleRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.PrescripcionRq;
import com.uniminuto.clinica.models.TarifaRs;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Expone la atención de una cita, las tarifas disponibles, las fórmulas y los PDF de fórmula y
 * prefactura.
 */
@Tag(name = "Atención clínica", description = "Veterinaria. La cuenta debe estar vinculada al veterinario asignado a la cita.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/atencion/{citaId}")
public interface AtencionApi {
    /** Consulta el paciente, sus antecedentes y la historia actual de la cita asignada. */
    @Operation(summary = "Consultar atención",
            description = "Muestra paciente, antecedentes e historia de la cita.")
    @GetMapping
    AtencionDetalleRs consultar(@PathVariable long citaId);

    /** Lista los servicios que se pueden seleccionar para cerrar la atención. */
    @Operation(summary = "Consultar tarifas de la cita")
    @GetMapping("/tarifas")
    List<TarifaRs> listarTarifas(@PathVariable long citaId);

    /**
     * Guarda los datos clínicos, marca la cita como atendida y devuelve el identificador de la
     * prefactura.
     */
    @Operation(summary = "Finalizar atención",
            description = "Envía una tarifa individual o una lista de tarifas distintas. Guarda la atención y devuelve el identificador de la prefactura.")
    @PostMapping("/finalizar")
    Map<String, Long> finalizar(@PathVariable long citaId, @RequestBody AtencionRq solicitud, Principal usuario);

    /** Registra uno o varios medicamentos de la cita y devuelve el identificador de la fórmula. */
    @Operation(summary = "Emitir fórmula médica",
            description = "La cita debe estar programada o confirmada. Permite incluir varios medicamentos sin descontar inventario.")
    @PostMapping("/formulas")
    Map<String, Long> formular(@PathVariable long citaId, @RequestBody PrescripcionRq solicitud);

    /** Consulta el saldo utilizable de un medicamento sin descontar unidades. */
    @Operation(summary = "Consultar existencias para la fórmula",
            description = "Devuelve las unidades disponibles para dispensar.")
    @GetMapping("/existencias/{medicamentoId}")
    Map<String, Long> existencias(@PathVariable long citaId, @PathVariable long medicamentoId);

    /** Descarga el PDF de una fórmula que pertenece a la cita. */
    @Operation(summary = "Descargar fórmula en PDF")
    @GetMapping(value = "/formulas/{formulaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> formula(@PathVariable long citaId, @PathVariable long formulaId) throws IOException;

    /** Descarga el PDF de la prefactura asociada a la cita. */
    @Operation(summary = "Descargar prefactura en PDF")
    @GetMapping(value = "/prefactura/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> prefactura(@PathVariable long citaId, @PathVariable long id) throws IOException;
}
