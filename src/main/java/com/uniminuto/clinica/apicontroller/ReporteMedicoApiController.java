package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.api.ReporteMedicoApi;
import com.uniminuto.clinica.service.ReporteMedicoService;
import com.uniminuto.clinica.util.ExportarReporte;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

/** Entrega en JSON, PDF o Excel el reporte médico calculado con los filtros solicitados. */
@RestController
@RequiredArgsConstructor
public class ReporteMedicoApiController implements ReporteMedicoApi {

    private final ReporteMedicoService reportes;

    @Override
    public ReporteRs consultar(LocalDate inicio, LocalDate fin,
                               Long medicoId,
                               Long especializacionId) {
        return reportes.consultar(inicio, fin, medicoId, especializacionId);
    }

    @Override
    public ResponseEntity<byte[]> descargar(String formato, LocalDate inicio,
                                            LocalDate fin, Long medicoId,
                                            Long especializacionId) throws IOException {
        ReporteRs reporte = consultar(inicio, fin, medicoId, especializacionId);
        return switch (formato) {
            case "pdf" -> descargar("gestion-medica.pdf", MediaType.APPLICATION_PDF, ExportarReporte.pdf(reporte));
            case "excel" -> descargar("gestion-medica.xlsx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    ExportarReporte.excel(reporte));
            default -> throw new BadRequestException("El formato debe ser pdf o excel.");
        };
    }

    private ResponseEntity<byte[]> descargar(String nombre, MediaType tipo, byte[] bytes) {
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre).body(bytes);
    }
}
