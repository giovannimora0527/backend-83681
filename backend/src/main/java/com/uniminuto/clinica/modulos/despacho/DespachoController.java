package com.uniminuto.clinica.modulos.despacho;

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
@RequestMapping("/despacho")
@Tag(name = "5. Despacho de fórmulas", description = "Exclusivo del perfil DESPACHO_MEDICO (validado en el backend). "
        + "Único módulo que descuenta inventario (lotes FEFO, sin lotes vencidos).")
public class DespachoController {

    private final DespachoService service;

    public DespachoController(DespachoService service) {
        this.service = service;
    }

    @Operation(summary = "Fórmulas pendientes de despacho; búsqueda por fórmula, cita, mascota o propietario")
    @GetMapping("/formulas")
    public List<Map<String, Object>> pendientes(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) String estado) {
        return service.pendientes(q, estado);
    }

    @Operation(summary = "Detalle de la fórmula: disponibilidad, alertas y lotes propuestos por FEFO")
    @GetMapping("/formulas/{id}")
    public Map<String, Object> formula(@PathVariable long id) {
        return service.formula(id);
    }

    @Operation(summary = "Marca la fórmula en preparación")
    @PostMapping("/formulas/{id}/preparar")
    public Map<String, Object> preparar(@PathVariable long id) {
        return service.preparar(id);
    }

    @Operation(summary = "Confirma el despacho (total o parcial) en una transacción",
            description = "Cuerpo opcional: {observaciones, items:[{formulaDetalleId, cantidad}]}. Sin items despacha lo disponible.")
    @PostMapping("/formulas/{id}/confirmar")
    public Map<String, Object> confirmar(@PathVariable long id, @RequestBody(required = false) Map<String, Object> body) {
        return service.confirmar(id, body);
    }

    @Operation(summary = "Cancela el despacho de una fórmula sin unidades dispensadas", description = "Cuerpo: {motivo}")
    @PostMapping("/formulas/{id}/cancelar")
    public Map<String, Object> cancelar(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.cancelar(id, body);
    }

    @Operation(summary = "Historial de cambios de estado de la fórmula")
    @GetMapping("/formulas/{id}/log")
    public List<Map<String, Object>> log(@PathVariable long id) {
        return service.logFormula(id);
    }

    @Operation(summary = "Historial de despachos con filtros")
    @GetMapping("/historial")
    public List<Map<String, Object>> historial(@RequestParam(required = false) String estado,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                               @RequestParam(required = false) String q) {
        return service.historial(estado, desde, hasta, q);
    }

    @Operation(summary = "Detalle de un despacho con lotes utilizados")
    @GetMapping("/{id}")
    public Map<String, Object> detalle(@PathVariable long id) {
        return service.detalleDespacho(id);
    }

    @Operation(summary = "Comprobante de despacho en PDF")
    @GetMapping("/{id}/comprobante/pdf")
    public ResponseEntity<byte[]> comprobante(@PathVariable long id) {
        return Http.pdf(service.comprobantePdf(id), "comprobante-despacho-" + id + ".pdf");
    }

    @Operation(summary = "Revierte un despacho (requiere autorización de un administrador)",
            description = "Cuerpo: {motivo, adminUsername, adminPassword}. Devuelve el stock a sus lotes y deja trazabilidad.")
    @PostMapping("/{id}/revertir")
    public Map<String, Object> revertir(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.revertir(id, body);
    }
}
