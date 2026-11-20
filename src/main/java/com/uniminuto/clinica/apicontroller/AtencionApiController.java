package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.api.AtencionApi;
import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.service.AtencionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class AtencionApiController implements AtencionApi {
    private final AtencionService service;
    public AtencionApiController(AtencionService service) { this.service = service; }
    @Override public ResponseEntity<AtencionMedica> finalizar(AtencionRq request, HttpServletRequest httpRequest) {
        Long userId = Long.valueOf(httpRequest.getAttribute("authenticatedUserId").toString());
        String role = httpRequest.getAttribute("authenticatedRole").toString();
        return ResponseEntity.status(201).body(service.finalizar(request, userId, role));
    }
    @Override public ResponseEntity<?> contexto(Integer mascotaId, HttpServletRequest request) {
        return ResponseEntity.ok(service.contexto(mascotaId,Long.valueOf(request.getAttribute("authenticatedUserId").toString()),request.getAttribute("authenticatedRole").toString()));
    }
    @Override public ResponseEntity<byte[]> pdf(Long atencionId, HttpServletRequest request) {
        byte[] body=service.pdf(atencionId,Long.valueOf(request.getAttribute("authenticatedUserId").toString()),request.getAttribute("authenticatedRole").toString());
        return ResponseEntity.ok().header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=atencion-"+atencionId+".pdf").contentType(org.springframework.http.MediaType.APPLICATION_PDF).body(body);
    }
}
