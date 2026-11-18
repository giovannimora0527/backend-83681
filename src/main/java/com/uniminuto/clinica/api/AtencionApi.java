package com.uniminuto.clinica.api;

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
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/atencion/{citaId}")
public interface AtencionApi {
    /** Consulta el paciente, sus antecedentes y la historia actual de la cita asignada. */
    @GetMapping
    AtencionDetalleRs consultar(@PathVariable long citaId);

    /** Lista los servicios que se pueden seleccionar para cerrar la atención. */
    @GetMapping("/tarifas")
    List<TarifaRs> listarTarifas(@PathVariable long citaId);

    /**
     * Guarda los datos clínicos, marca la cita como atendida y devuelve el identificador de la
     * prefactura.
     */
    @PostMapping("/finalizar")
    Map<String, Long> finalizar(@PathVariable long citaId, @RequestBody AtencionRq solicitud, Principal usuario);

    /** Registra uno o varios medicamentos de la cita y devuelve el identificador de la fórmula. */
    @PostMapping("/formulas")
    Map<String, Long> formular(@PathVariable long citaId, @RequestBody PrescripcionRq solicitud);

    /** Consulta el saldo utilizable de un medicamento sin descontar unidades. */
    @GetMapping("/existencias/{medicamentoId}")
    Map<String, Long> existencias(@PathVariable long citaId, @PathVariable long medicamentoId);

    /** Descarga el PDF de una fórmula que pertenece a la cita. */
    @GetMapping(value = "/formulas/{formulaId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> formula(@PathVariable long citaId, @PathVariable long formulaId) throws IOException;

    /** Descarga el PDF de la prefactura asociada a la cita. */
    @GetMapping(value = "/prefactura/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> prefactura(@PathVariable long citaId, @PathVariable long id) throws IOException;
}
