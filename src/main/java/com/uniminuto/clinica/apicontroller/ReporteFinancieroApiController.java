package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.api.ReporteFinancieroApi;
import com.uniminuto.clinica.service.ReporteFinancieroService;
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
            case "pdf" -> descargar("gestion-financiera.pdf", MediaType.APPLICATION_PDF, InformePdf.generar(reporte,
                    ExportarReporte.contexto(inicio, fin, medicoId, especializacionId) + "  |  Agrupación: " + periodo));
            case "excel" -> descargar("gestion-financiera.xlsx",
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    ExportarReporte.excel(reporte, "Financiero",
                            ExportarReporte.contexto(inicio, fin, medicoId, especializacionId)
                                    + "  |  Agrupación: " + periodo, indicadores(reporte)));
            default -> throw new BadRequestException("El formato debe ser pdf o excel.");
        };
    }

    /** Muestra los totales de facturación, impuestos y recaudo en la cabecera del Excel. */
    private List<ExportarReporte.Indicador> indicadores(ReporteRs reporte) {
        return List.of(
                new ExportarReporte.Indicador("Facturas generadas", total(reporte, "Facturación", 2), false),
                new ExportarReporte.Indicador("Total facturado", total(reporte, "Facturación", 7), true),
                new ExportarReporte.Indicador("Recaudo neto", total(reporte, "Recaudo neto", 7), true),
                new ExportarReporte.Indicador("IVA facturado", total(reporte, "Facturación", 8), true));
    }

    /** Obtiene un total existente en el reporte sin volver a calcularlo. */
    private Number total(ReporteRs reporte, String concepto, int columna) {
        return reporte.filas().stream().filter(fila -> concepto.equals(fila.get(0)) && "TOTAL".equals(fila.get(1)))
                .map(fila -> (Number) fila.get(columna)).findFirst().orElse(0);
    }

    /** Prepara el archivo y los encabezados de la respuesta de descarga. */
    private ResponseEntity<byte[]> descargar(String nombre, MediaType tipo, byte[] bytes) {
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre).body(bytes);
    }
}

