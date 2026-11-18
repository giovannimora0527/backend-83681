package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.models.ReporteRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Expone la vista previa y descarga en PDF o Excel del reporte financiero. Permite filtrar por
 * fechas, veterinario y especialidad, y agrupar por día, mes o año.
 */
@Tag(name = "Reportes financieros", description = "Administración y Recepción. Consulta lo facturado, los pagos, las devoluciones y el recaudo neto.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/reportes/financieros")
public interface ReporteFinancieroApi {

    /** Devuelve la vista previa del reporte. La agrupación predeterminada es por mes. */
    @Operation(summary = "Consultar vista previa del reporte financiero",
            description = "Incluye ambas fechas. periodo admite dia, mes o anio.")
    @GetMapping
    ReporteRs consultar(@RequestParam LocalDate inicio, @RequestParam LocalDate fin,
                         @RequestParam(defaultValue = "mes") String periodo,
                         @RequestParam(required = false) Long medicoId,
                         @RequestParam(required = false) Long especializacionId);

    /** Descarga el reporte en PDF o Excel con los mismos filtros de la vista previa. */
    @Operation(summary = "Descargar reporte financiero en PDF o Excel",
            description = "Usa pdf o excel y conserva los filtros de la consulta previa.")
    @GetMapping("/{formato}")
    ResponseEntity<byte[]> descargar(@PathVariable String formato, @RequestParam LocalDate inicio,
                                     @RequestParam LocalDate fin, @RequestParam(defaultValue = "mes") String periodo,
                                     @RequestParam(required = false) Long medicoId,
                                     @RequestParam(required = false) Long especializacionId) throws IOException;
}
