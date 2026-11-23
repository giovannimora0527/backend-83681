package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/facturacion")
public interface FacturacionApi {
    @GetMapping(value = "/tarifas", produces = "application/json")
    ResponseEntity<List<TarifaServicioRs>> tarifas();

    @GetMapping(value = "/tarifas/admin", produces = "application/json")
    ResponseEntity<List<TarifaServicioRs>> todasLasTarifas();

    @PostMapping(value = "/tarifas", consumes = "application/json", produces = "application/json")
    ResponseEntity<TarifaServicioRs> crearTarifa(@RequestBody TarifaServicioRq request);

    @PutMapping(value = "/tarifas/{id}", consumes = "application/json", produces = "application/json")
    ResponseEntity<TarifaServicioRs> actualizarTarifa(@PathVariable Long id, @RequestBody TarifaServicioRq request);

    @PostMapping(value = "/tarifas/{id}/estado", produces = "application/json")
    ResponseEntity<TarifaServicioRs> cambiarEstadoTarifa(@PathVariable Long id, @RequestParam Boolean activo);

    @PostMapping(value = "/facturas/vista-previa", consumes = "application/json", produces = "application/json")
    ResponseEntity<FacturaRs> vistaPrevia(@RequestBody FacturarRq request);

    @PostMapping(value = "/facturas", consumes = "application/json", produces = "application/json")
    ResponseEntity<FacturaRs> facturar(@RequestBody FacturarRq request);

    @GetMapping(value = "/facturas/{id}", produces = "application/json")
    ResponseEntity<FacturaRs> obtener(@PathVariable Long id);

    @GetMapping(value = "/facturas/{id}.pdf", produces = "application/pdf")
    ResponseEntity<byte[]> descargarPdf(@PathVariable Long id);

    @PostMapping(value = "/facturas/{id}/pagos", consumes = "application/json", produces = "application/json")
    ResponseEntity<FacturaRs> registrarPago(@PathVariable Long id, @RequestBody PagoFacturaRq request);

    @PostMapping(value = "/facturas/{id}/anular", produces = "application/json")
    ResponseEntity<FacturaRs> anular(@PathVariable Long id, @RequestParam String motivo);
}
