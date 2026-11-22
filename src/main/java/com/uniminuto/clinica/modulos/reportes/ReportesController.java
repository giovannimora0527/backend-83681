package com.uniminuto.clinica.modulos.reportes;

import com.uniminuto.clinica.support.Http;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@Tag(name = "4. Reportes", description = "Solo ADMINISTRADOR. Cada reporte admite vista previa (JSON), PDF y Excel con los mismos filtros. "
        + "Filtros opcionales por reporte: estado, tipo, metodo, medicoId, especializacionId, medicamentoId.")
public class ReportesController {

    private final ReportesService service;

    public ReportesController(ReportesService service) {
        this.service = service;
    }

    @Operation(summary = "Catálogo de reportes disponibles (financieros, inventario, gestión médica)")
    @GetMapping
    public List<Map<String, Object>> catalogo() {
        return service.catalogo();
    }

    @Operation(summary = "Vista previa del reporte")
    @GetMapping("/{clave}")
    public Map<String, Object> vistaPrevia(@PathVariable String clave,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                           @RequestParam Map<String, String> filtros) {
        return service.vistaPrevia(clave, desde, hasta, filtros);
    }

    @Operation(summary = "Reporte en PDF")
    @GetMapping("/{clave}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String clave,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                      @RequestParam Map<String, String> filtros) {
        return Http.pdf(service.pdf(clave, desde, hasta, filtros), "reporte-" + clave + ".pdf");
    }

    @Operation(summary = "Reporte en Excel (.xlsx)")
    @GetMapping("/{clave}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable String clave,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                        @RequestParam Map<String, String> filtros) {
        return Http.xlsx(service.excel(clave, desde, hasta, filtros), "reporte-" + clave + ".xlsx");
    }
}
