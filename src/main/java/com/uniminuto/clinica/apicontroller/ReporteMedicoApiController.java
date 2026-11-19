package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.api.ReporteMedicoApi;
import com.uniminuto.clinica.service.ReporteMedicoService;
import com.uniminuto.clinica.util.ExportarReporte;
import com.uniminuto.clinica.util.InformePdf;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** Entrega en JSON, PDF o Excel el reporte médico calculado con los filtros solicitados. */
@RestController
@RequiredArgsConstructor
public class ReporteMedicoApiController implements ReporteMedicoApi {

    private final ReporteMedicoService reportes;

    /** Consulta el reporte de citas y atenciones. */
    @Override
    public ReporteRs consultar(LocalDate inicio, LocalDate fin,
                               Long medicoId,
                               Long especializacionId) {
        return reportes.consultar(inicio, fin, medicoId, especializacionId);
    }

    /** Prepara el archivo y los encabezados de la respuesta de descarga. */
    @Override
    public ResponseEntity<byte[]> descargar(String formato, LocalDate inicio,
                                            LocalDate fin, Long medicoId,
                                            Long especializacionId) throws IOException {
        ReporteRs reporte = consultar(inicio, fin, medicoId, especializacionId);
        return switch (formato) {
            case "pdf" -> descargar("gestion-medica.pdf", MediaType.APPLICATION_PDF, InformePdf.generar(reporte,
                    ExportarReporte.contexto(inicio, fin, medicoId, especializacionId)));
            case "excel" -> descargar("gestion-medica.xlsx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    ExportarReporte.excel(reporte, "Gestión médica",
                            ExportarReporte.contexto(inicio, fin, medicoId, especializacionId),
                            indicadores(reporte)));
            default -> throw new BadRequestException("El formato debe ser pdf o excel.");
        };
    }

    /** Muestra los totales de la consulta en la cabecera del Excel. */
    private List<ExportarReporte.Indicador> indicadores(ReporteRs reporte) {
        return reporte.filas().stream().filter(fila -> "TOTAL".equals(fila.get(1))).findFirst()
                .map(fila -> List.of(
                        new ExportarReporte.Indicador("Total de citas", (Number) fila.get(4), false),
                        new ExportarReporte.Indicador("Citas atendidas", (Number) fila.get(5), false),
                        new ExportarReporte.Indicador("Mascotas atendidas", (Number) fila.get(8), false),
                        new ExportarReporte.Indicador("Consultas realizadas", (Number) fila.get(11), false)))
                .orElseGet(List::of);
    }

    /** Prepara el archivo y los encabezados de la respuesta de descarga. */
    private ResponseEntity<byte[]> descargar(String nombre, MediaType tipo, byte[] bytes) {
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre).body(bytes);
    }
}
