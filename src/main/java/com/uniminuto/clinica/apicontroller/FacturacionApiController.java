package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FacturacionApi;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.serviceimpl.FacturacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class FacturacionApiController implements FacturacionApi {
    private final FacturacionService service;
    public FacturacionApiController(FacturacionService service) { this.service = service; }
    @Override public ResponseEntity<List<TarifaServicioRs>> tarifas() { return ResponseEntity.ok(service.tarifas()); }
    @Override public ResponseEntity<List<TarifaServicioRs>> todasLasTarifas() { return ResponseEntity.ok(service.todasLasTarifas()); }
    @Override public ResponseEntity<TarifaServicioRs> crearTarifa(TarifaServicioRq request) { return ResponseEntity.status(201).body(service.crearTarifa(request)); }
    @Override public ResponseEntity<TarifaServicioRs> actualizarTarifa(Long id, TarifaServicioRq request) { return ResponseEntity.ok(service.actualizarTarifa(id, request)); }
    @Override public ResponseEntity<TarifaServicioRs> cambiarEstadoTarifa(Long id, Boolean activo) { return ResponseEntity.ok(service.cambiarEstadoTarifa(id, activo)); }
    @Override public ResponseEntity<FacturaRs> vistaPrevia(FacturarRq request) { return ResponseEntity.ok(service.vistaPrevia(request)); }
    @Override public ResponseEntity<FacturaRs> facturar(FacturarRq request) { return ResponseEntity.status(201).body(service.facturar(request)); }
    @Override public ResponseEntity<FacturaRs> obtener(Long id) { return ResponseEntity.ok(service.obtener(id)); }
    @Override public ResponseEntity<byte[]> descargarPdf(Long id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF).body(service.generarPdf(id));
    }
    @Override public ResponseEntity<FacturaRs> registrarPago(Long id, PagoFacturaRq request) { return ResponseEntity.ok(service.registrarPago(id, request)); }
    @Override public ResponseEntity<FacturaRs> anular(Long id, String motivo) { return ResponseEntity.ok(service.anular(id, motivo)); }
}
