package com.uniminuto.clinica.modulos.facturacion;

import com.uniminuto.clinica.support.Http;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/facturas")
@Tag(name = "3c. Facturación", description = "ADMINISTRADOR y RECEPCION. Anulaciones y descuentos: solo ADMINISTRADOR.")
public class FacturacionController {

    private final FacturacionService service;

    public FacturacionController(FacturacionService service) {
        this.service = service;
    }

    @Operation(summary = "Citas atendidas pendientes de facturar (con estado de despacho de la fórmula)")
    @GetMapping("/pendientes")
    public List<Map<String, Object>> pendientes() {
        return service.pendientesDeFacturar();
    }

    @Operation(summary = "Historial de facturación con filtros (cliente, mascota, estado, rango de fechas, texto)")
    @GetMapping
    public List<Map<String, Object>> listar(@RequestParam(required = false) Long clienteId,
                                            @RequestParam(required = false) Long mascotaId,
                                            @RequestParam(required = false) String estado,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                            @RequestParam(required = false) String q) {
        return service.listar(clienteId, mascotaId, estado, desde, hasta, q);
    }

    @Operation(summary = "Detalle de la factura con líneas, pagos y trazabilidad")
    @GetMapping("/{id}")
    public Map<String, Object> detalle(@PathVariable long id) {
        return service.detalle(id);
    }

    @Operation(summary = "Genera la factura de una cita atendida (una factura vigente por cita)",
            description = "Cuerpo opcional: {descuento}. Solo ADMINISTRADOR puede aplicar descuento.")
    @PostMapping("/cita/{citaId}")
    public Map<String, Object> generar(@PathVariable long citaId, @RequestBody(required = false) Map<String, Object> body) {
        return service.generar(citaId, body);
    }

    @Operation(summary = "Registra un pago total o parcial", description = "Cuerpo: {monto, metodo (EFECTIVO|TARJETA|TRANSFERENCIA|OTRO), referencia}")
    @PostMapping("/{id}/pagos")
    public Map<String, Object> pagar(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.pagar(id, body);
    }

    @Operation(summary = "Anula la factura (solo ADMINISTRADOR, motivo obligatorio)", description = "Cuerpo: {motivo}")
    @PostMapping("/{id}/anular")
    public Map<String, Object> anular(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.anular(id, body);
    }

    @Operation(summary = "Factura en PDF")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable long id) {
        return Http.pdf(service.pdf(id), "factura-" + id + ".pdf");
    }
}
