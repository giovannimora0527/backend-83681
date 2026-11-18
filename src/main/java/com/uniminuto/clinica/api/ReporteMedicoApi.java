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
 * Expone la vista previa y descarga en PDF o Excel del reporte médico. Los filtros son el rango de
 * fechas, el veterinario y la especialidad.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/reportes/medicos")
public interface ReporteMedicoApi {

    /** Devuelve la vista previa del reporte. Se incluyen las fechas inicial y final. */
    @GetMapping
    ReporteRs consultar(@RequestParam LocalDate inicio, @RequestParam LocalDate fin,
                         @RequestParam(required = false) Long medicoId,
                         @RequestParam(required = false) Long especializacionId);

    /** Descarga el reporte en PDF o Excel aplicando los filtros de la consulta. */
    @GetMapping("/{formato}")
    ResponseEntity<byte[]> descargar(@PathVariable String formato, @RequestParam LocalDate inicio,
                                     @RequestParam LocalDate fin, @RequestParam(required = false) Long medicoId,
                                     @RequestParam(required = false) Long especializacionId) throws IOException;
}
