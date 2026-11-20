package com.uniminuto.clinica.api;
import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.models.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RequestMapping("/facturas") @CrossOrigin(origins="*", allowedHeaders="*")
public interface FacturacionApi {
    @PostMapping("/atencion/{atencionId}") ResponseEntity<Factura> crear(@PathVariable Long atencionId, @RequestBody FacturaRq request, HttpServletRequest httpRequest);
    @PostMapping("/{facturaId}/pagos") ResponseEntity<Factura> pagar(@PathVariable Long facturaId, @RequestBody FacturaPagoRq request, HttpServletRequest httpRequest);
    @PostMapping("/{facturaId}/anular") ResponseEntity<Factura> anular(@PathVariable Long facturaId, @RequestBody java.util.Map<String,String> request, HttpServletRequest httpRequest);
    @GetMapping("/cliente/{clienteId}") ResponseEntity<?> porCliente(@PathVariable Long clienteId);
    @GetMapping("/mascota/{mascotaId}") ResponseEntity<?> porMascota(@PathVariable Integer mascotaId);
    @GetMapping("/{facturaId}") ResponseEntity<?> detalle(@PathVariable Long facturaId);
    @GetMapping(value="/{facturaId}/pdf", produces="application/pdf") ResponseEntity<byte[]> pdf(@PathVariable Long facturaId);
}
