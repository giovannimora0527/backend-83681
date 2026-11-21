package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AtencionApi;
import com.uniminuto.clinica.models.AtencionDetalleRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.PrescripcionRq;
import com.uniminuto.clinica.models.TarifaRs;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.service.TarifaService;
import com.uniminuto.clinica.service.AtencionService;
import com.uniminuto.clinica.service.FacturacionService;
import com.uniminuto.clinica.util.FacturaPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Entrega los datos de la atención, los identificadores de fórmula y prefactura y sus archivos
 * PDF.
 */
@RestController
@RequiredArgsConstructor
public class AtencionApiController implements AtencionApi {

    private final AtencionService atenciones;
    private final FacturacionService facturacion;
    private final CitaService citas;
    private final TarifaService tarifas;

    /** Devuelve los datos clínicos disponibles para la cita solicitada. */
    @Override
    public AtencionDetalleRs consultar(long citaId) {
        return atenciones.consultar(citaId);
    }

    /** Consulta las tarifas. */
    @Override
    public List<TarifaRs> listarTarifas(long citaId) {
        return tarifas.listarParaCita(citaId);
    }

    /** Solicita cerrar la atención y guardar los datos clínicos de la cita. */
    @Override
    public Map<String, Long> finalizar(long citaId, AtencionRq solicitud, Principal usuario) {
        return Map.of("prefacturaId", atenciones.finalizar(citaId, solicitud, Long.parseLong(usuario.getName())));
    }

    /** Guarda la prescripción asociada a la atención. */
    @Override
    public Map<String, Long> formular(long citaId, PrescripcionRq solicitud) {
        return Map.of("formulaId", atenciones.formular(citaId, solicitud));
    }

    /** Consulta las cantidades disponibles para el medicamento indicado. */
    @Override
    public Map<String, Long> existencias(long citaId, long medicamentoId) {
        return Map.of("disponible", atenciones.existencias(citaId, medicamentoId));
    }

    /** Prepara el documento de la fórmula para la cita indicada. */
    @Override
    public ResponseEntity<byte[]> formula(long citaId, long formulaId) throws IOException {
        return pdf("formula-" + formulaId, atenciones.formula(citaId, formulaId));
    }

    /** Obtiene el documento de la prefactura asociada a la cita. */
    @Override
    public ResponseEntity<byte[]> prefactura(long citaId, long id) throws IOException {
        citas.detalle(citaId);
        return pdf("prefactura-" + id, FacturaPdf.generar(facturacion.documento(id, citaId)));
    }

    /** Prepara la respuesta con el documento PDF solicitado. */
    private ResponseEntity<byte[]> pdf(String nombre, byte[] cuerpo) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre + ".pdf").body(cuerpo);
    }
}
