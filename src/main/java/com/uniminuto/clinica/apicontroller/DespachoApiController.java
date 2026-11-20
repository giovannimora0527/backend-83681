package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.api.DespachoApi;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.models.ConfirmarDespachoRq;
import com.uniminuto.clinica.service.DespachoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class DespachoApiController implements DespachoApi {
    private final DespachoService despachoService;
    public DespachoApiController(DespachoService despachoService) { this.despachoService = despachoService; }
    @Override public ResponseEntity<?> pendientes(Long formulaId,Integer mascotaId,String propietario,java.time.LocalDateTime desde,java.time.LocalDateTime hasta) { return ResponseEntity.ok(despachoService.buscarPendientes(formulaId,mascotaId,propietario,desde,hasta)); }
    @Override public ResponseEntity<?> historial(java.time.LocalDateTime desde,java.time.LocalDateTime hasta,Integer mascotaId,String propietario) { return ResponseEntity.ok(despachoService.historial(desde,hasta,mascotaId,propietario)); }
    @Override public ResponseEntity<java.util.List<DespachoDetalle>> detalles(Long despachoId) { return ResponseEntity.ok(despachoService.detalles(despachoId)); }
    @Override public ResponseEntity<Despacho> confirmar(ConfirmarDespachoRq request, HttpServletRequest httpRequest) {
        Object id = httpRequest.getAttribute("authenticatedUserId");
        Long usuarioId = id == null ? null : Long.valueOf(id.toString());
        return ResponseEntity.status(201).body(despachoService.confirmar(request, usuarioId));
    }
    @Override public ResponseEntity<Despacho> revertir(Long despachoId, java.util.Map<String,String> request, HttpServletRequest httpRequest) {
        if (!"ADMINISTRADOR".equals(httpRequest.getAttribute("authenticatedRole")))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Solo un administrador puede autorizar una reversión.");
        Long usuarioId = Long.valueOf(httpRequest.getAttribute("authenticatedUserId").toString());
        return ResponseEntity.ok(despachoService.revertir(despachoId, request == null ? null : request.get("motivo"), usuarioId));
    }
}
