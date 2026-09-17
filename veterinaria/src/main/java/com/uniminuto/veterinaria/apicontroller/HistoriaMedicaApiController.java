package com.uniminuto.veterinaria.apicontroller;

import com.uniminuto.veterinaria.api.HistoriaMedicaApi;
import com.uniminuto.veterinaria.entity.HistoriaMedica;
import com.uniminuto.veterinaria.service.HistoriaMedicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST encargado de atender las peticiones HTTP de la entidad HistoriaMedica
 * implementando la interfaz {@link HistoriaMedicaApi}.
 */
@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    private final HistoriaMedicaService service;

    /**
     * Inyección de dependencias por constructor para la capa de servicio de Historia Médica.
     *
     * @param service Instancia de {@link HistoriaMedicaService}.
     */
    public HistoriaMedicaApiController(HistoriaMedicaService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<HistoriaMedica>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @Override
    public ResponseEntity<List<HistoriaMedica>> filtrarPorFecha(String inicio, String fin) {
        return ResponseEntity.ok(service.filtrarPorFecha(inicio, fin));
    }

    @Override
    public ResponseEntity<HistoriaMedica> crearHistoriaMedica(HistoriaMedica historiaMedica) {
        return ResponseEntity.ok(service.guardarHistoriaMedica(historiaMedica));
    }

    @Override
    public ResponseEntity<HistoriaMedica> actualizarHistoriaMedica(Long id, HistoriaMedica historiaMedica) {
        return ResponseEntity.ok(service.actualizarHistoriaMedica(id, historiaMedica));
    }
}