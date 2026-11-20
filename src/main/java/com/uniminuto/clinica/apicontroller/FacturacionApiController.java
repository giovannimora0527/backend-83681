package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.api.FacturacionApi;
import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.service.FacturacionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class FacturacionApiController implements FacturacionApi {
    private final FacturacionService service;
    public FacturacionApiController(FacturacionService service) { this.service=service; }
    @Override public ResponseEntity<Factura> crear(Long atencionId, FacturaRq request, HttpServletRequest httpRequest) {
        String role=httpRequest.getAttribute("authenticatedRole").toString();
        if(request!=null&&request.descuento()!=null&&request.descuento().signum()>0&&!"ADMINISTRADOR".equals(role))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Solo un administrador puede autorizar descuentos.");
        return ResponseEntity.status(201).body(service.crear(atencionId,request,Long.valueOf(httpRequest.getAttribute("authenticatedUserId").toString())));
    }
    @Override public ResponseEntity<Factura> pagar(Long facturaId, FacturaPagoRq request, HttpServletRequest req) { return ResponseEntity.ok(service.pagar(facturaId,request,Long.valueOf(req.getAttribute("authenticatedUserId").toString()))); }
    @Override public ResponseEntity<Factura> anular(Long facturaId, java.util.Map<String,String> request, HttpServletRequest req) { return ResponseEntity.ok(service.anular(facturaId,request==null?null:request.get("motivo"),Long.valueOf(req.getAttribute("authenticatedUserId").toString()))); }
    @Override public ResponseEntity<?> porCliente(Long clienteId) { return ResponseEntity.ok(service.porCliente(clienteId)); }
    @Override public ResponseEntity<?> porMascota(Integer mascotaId) { return ResponseEntity.ok(service.porMascota(mascotaId)); }
    @Override public ResponseEntity<?> detalle(Long facturaId) { return ResponseEntity.ok(service.detalle(facturaId)); }
    @Override public ResponseEntity<byte[]> pdf(Long facturaId) { return ResponseEntity.ok().header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=factura-"+facturaId+".pdf").contentType(org.springframework.http.MediaType.APPLICATION_PDF).body(service.pdf(facturaId)); }
}
