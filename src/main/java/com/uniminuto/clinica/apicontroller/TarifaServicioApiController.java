package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.api.TarifaServicioApi;
import com.uniminuto.clinica.entity.TarifaServicio;
import com.uniminuto.clinica.models.TarifaServicioRq;
import com.uniminuto.clinica.service.TarifaServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
public class TarifaServicioApiController implements TarifaServicioApi {
    private final TarifaServicioService service;
    public TarifaServicioApiController(TarifaServicioService service) { this.service = service; }
    @Override public ResponseEntity<List<TarifaServicio>> listar() { return ResponseEntity.ok(service.listar()); }
    @Override public ResponseEntity<List<TarifaServicio>> historial(String nombre) { return ResponseEntity.ok(service.historial(nombre)); }
    @Override public ResponseEntity<TarifaServicio> crear(TarifaServicioRq request) { return ResponseEntity.status(201).body(service.crear(request)); }
    @Override public ResponseEntity<TarifaServicio> actualizar(Long id, TarifaServicioRq request) { return ResponseEntity.ok(service.actualizar(id, request)); }
}
