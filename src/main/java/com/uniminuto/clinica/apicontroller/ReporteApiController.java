package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.ReporteApi;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.serviceimpl.ReporteService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
public class ReporteApiController implements ReporteApi {
    private final ReporteService service;
    public ReporteApiController(ReporteService service) { this.service = service; }
    @Override public ResponseEntity<ReporteCitasRs> citas(LocalDate desde, LocalDate hasta) { return ResponseEntity.ok(service.citas(desde, hasta)); }
    @Override public ResponseEntity<byte[]> citasExcel(LocalDate desde, LocalDate hasta) {
        return descarga(service.citasExcel(service.citas(desde,hasta)), "reporte-citas.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    @Override public ResponseEntity<byte[]> citasPdf(LocalDate desde, LocalDate hasta) {
        return descarga(service.citasPdf(service.citas(desde,hasta)), "reporte-citas.pdf", MediaType.APPLICATION_PDF_VALUE);
    }
    @Override public ResponseEntity<ReporteFacturacionRs> facturacion(LocalDate desde, LocalDate hasta) { return ResponseEntity.ok(service.facturacion(desde,hasta)); }
    @Override public ResponseEntity<byte[]> facturacionExcel(LocalDate desde, LocalDate hasta) {
        return descarga(service.facturacionExcel(service.facturacion(desde,hasta)), "reporte-facturacion.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    @Override public ResponseEntity<byte[]> facturacionPdf(LocalDate desde, LocalDate hasta) {
        return descarga(service.facturacionPdf(service.facturacion(desde,hasta)), "reporte-facturacion.pdf", MediaType.APPLICATION_PDF_VALUE);
    }
    @Override public ResponseEntity<ReporteInventarioRs> inventario() { return ResponseEntity.ok(service.inventario()); }
    @Override public ResponseEntity<byte[]> inventarioExcel() {
        return descarga(service.inventarioExcel(service.inventario()), "reporte-inventario.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    @Override public ResponseEntity<byte[]> inventarioPdf() {
        return descarga(service.inventarioPdf(service.inventario()), "reporte-inventario.pdf", MediaType.APPLICATION_PDF_VALUE);
    }
    @Override public ResponseEntity<ReporteMedicosRs> medicos(LocalDate desde, LocalDate hasta) { return ResponseEntity.ok(service.medicos(desde,hasta)); }
    @Override public ResponseEntity<byte[]> medicosExcel(LocalDate desde, LocalDate hasta) {
        return descarga(service.medicosExcel(service.medicos(desde,hasta)), "reporte-medicos.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
    @Override public ResponseEntity<byte[]> medicosPdf(LocalDate desde, LocalDate hasta) {
        return descarga(service.medicosPdf(service.medicos(desde,hasta)), "reporte-medicos.pdf", MediaType.APPLICATION_PDF_VALUE);
    }
    private ResponseEntity<byte[]> descarga(byte[] content, String filename, String type) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(content);
    }
}
