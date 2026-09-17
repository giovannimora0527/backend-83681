package com.uniminuto.veterinaria.apicontroller;

import com.uniminuto.veterinaria.api.CitaApi;
import com.uniminuto.veterinaria.entity.Cita;
import com.uniminuto.veterinaria.service.CitaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para Citas implementando {@link CitaApi}.
 */
@RestController
public class CitaApiController implements CitaApi {

    private final CitaService service;

    public CitaApiController(CitaService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Cita>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @Override
    public ResponseEntity<List<Cita>> filtrarPorFecha(String inicio, String fin) {
        return ResponseEntity.ok(service.filtrarPorFecha(inicio, fin));
    }

    @Override
    public ResponseEntity<Cita> crearCita(Cita cita) {
        return ResponseEntity.ok(service.guardarCita(cita));
    }

    @Override
    public ResponseEntity<Cita> actualizarCita(Long id, Cita cita) {
        return ResponseEntity.ok(service.actualizarCita(id, cita));
    }
}