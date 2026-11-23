package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.ReporteCitasRs;
import com.uniminuto.clinica.models.ReporteFacturacionRs;
import com.uniminuto.clinica.models.ReporteInventarioRs;
import com.uniminuto.clinica.models.ReporteMedicosRs;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RequestMapping("/reportes")
public interface ReporteApi {
    @GetMapping(value = "/citas", produces = "application/json")
    ResponseEntity<ReporteCitasRs> citas(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/citas.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> citasExcel(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/citas.pdf", produces = "application/pdf")
    ResponseEntity<byte[]> citasPdf(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/facturacion", produces = "application/json")
    ResponseEntity<ReporteFacturacionRs> facturacion(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/facturacion.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> facturacionExcel(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/facturacion.pdf", produces = "application/pdf")
    ResponseEntity<byte[]> facturacionPdf(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/inventario", produces = "application/json")
    ResponseEntity<ReporteInventarioRs> inventario();
    @GetMapping(value = "/inventario.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> inventarioExcel();
    @GetMapping(value = "/inventario.pdf", produces = "application/pdf")
    ResponseEntity<byte[]> inventarioPdf();
    @GetMapping(value = "/medicos", produces = "application/json")
    ResponseEntity<ReporteMedicosRs> medicos(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/medicos.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> medicosExcel(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
    @GetMapping(value = "/medicos.pdf", produces = "application/pdf")
    ResponseEntity<byte[]> medicosPdf(@RequestParam LocalDate desde, @RequestParam LocalDate hasta);
}
