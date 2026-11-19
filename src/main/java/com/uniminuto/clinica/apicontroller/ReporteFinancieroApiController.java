package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.api.ReporteFinancieroApi;
import com.uniminuto.clinica.service.ReporteFinancieroService;
import com.uniminuto.clinica.util.ExportarReporte;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

/** Entrega en JSON, PDF o Excel el reporte financiero calculado con los mismos filtros. */
@RestController
@RequiredArgsConstructor
public class ReporteFinancieroApiController implements ReporteFinancieroApi {

    private final ReporteFinancieroService reportes;

    /** Consulta el resumen de facturación y recaudo. */
    @Override
    public ReporteRs consultar(LocalDate inicio, LocalDate fin, String periodo,
                               Long medicoId,
                               Long especializacionId) {
        return reportes.consultar(inicio, fin, periodo, medicoId, especializacionId);
    }

    /** Prepara el archivo y los encabezados de la respuesta de descarga. */
    @Override
    public ResponseEntity<byte[]> descargar(String formato, LocalDate inicio,
                                            LocalDate fin, String periodo, Long medicoId,
                                            Long especializacionId) throws IOException {
        ReporteRs reporte = consultar(inicio, fin, periodo, medicoId, especializacionId);
        return switch (formato) {
            case "pdf" -> descargar("gestion-financiera.pdf", MediaType.APPLICATION_PDF, ExportarReporte.pdf(reporte));
            case "excel" -> descargar("gestion-financiera.xlsx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    ExportarReporte.excel(reporte));
            default -> throw new BadRequestException("El formato debe ser pdf o excel.");
        };
    }

    /** Prepara el archivo y los encabezados de la respuesta de descarga. */
    private ResponseEntity<byte[]> descargar(String nombre, MediaType tipo, byte[] bytes) {
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre).body(bytes);
    }
}

