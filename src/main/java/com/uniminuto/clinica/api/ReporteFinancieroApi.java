package com.uniminuto.clinica.api;

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
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/reportes/financieros")
public interface ReporteFinancieroApi {

    /** Devuelve la vista previa del reporte. La agrupación predeterminada es por mes. */
    @GetMapping
    ReporteRs consultar(@RequestParam LocalDate inicio, @RequestParam LocalDate fin,
                         @RequestParam(defaultValue = "mes") String periodo,
                         @RequestParam(required = false) Long medicoId,
                         @RequestParam(required = false) Long especializacionId);

    /** Descarga el reporte en PDF o Excel con los mismos filtros de la vista previa. */
    @GetMapping("/{formato}")
    ResponseEntity<byte[]> descargar(@PathVariable String formato, @RequestParam LocalDate inicio,
                                     @RequestParam LocalDate fin, @RequestParam(defaultValue = "mes") String periodo,
                                     @RequestParam(required = false) Long medicoId,
                                     @RequestParam(required = false) Long especializacionId) throws IOException;
}
