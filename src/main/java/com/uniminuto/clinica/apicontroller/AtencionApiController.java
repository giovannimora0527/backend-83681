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
import com.uniminuto.clinica.util.ExportarReporte;
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

    @Override
    public AtencionDetalleRs consultar(long citaId) {
        return atenciones.consultar(citaId);
    }

    @Override
    public List<TarifaRs> listarTarifas(long citaId) {
        return tarifas.listarParaCita(citaId);
    }

    @Override
    public Map<String, Long> finalizar(long citaId, AtencionRq solicitud, Principal usuario) {
        return Map.of("prefacturaId", atenciones.finalizar(citaId, solicitud, Long.parseLong(usuario.getName())));
    }

    @Override
    public Map<String, Long> formular(long citaId, PrescripcionRq solicitud) {
        return Map.of("formulaId", atenciones.formular(citaId, solicitud));
    }

    @Override
    public Map<String, Long> existencias(long citaId, long medicamentoId) {
        return Map.of("disponible", atenciones.existencias(citaId, medicamentoId));
    }

    @Override
    public ResponseEntity<byte[]> formula(long citaId, long formulaId) throws IOException {
        return pdf("formula-" + formulaId, ExportarReporte.pdf(atenciones.formula(citaId, formulaId)));
    }

    @Override
    public ResponseEntity<byte[]> prefactura(long citaId, long id) throws IOException {
        citas.detalle(citaId);
        return pdf("prefactura-" + id, ExportarReporte.pdf(facturacion.documento(id, citaId)));
    }

    private ResponseEntity<byte[]> pdf(String nombre, byte[] cuerpo) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre + ".pdf").body(cuerpo);
    }
}
