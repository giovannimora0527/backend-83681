package com.uniminuto.veterinaria.apicontroller;

import com.uniminuto.veterinaria.api.FormulaMedicaApi;
import com.uniminuto.veterinaria.entity.FormulaMedica;
import com.uniminuto.veterinaria.service.FormulaMedicaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST encargado de atender las peticiones HTTP de la entidad FormulaMedica
 * implementando la interfaz {@link FormulaMedicaApi}.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    private final FormulaMedicaService service;

    /**
     * Inyección de dependencias por constructor para la capa de servicio de Fórmulas Médicas.
     *
     * @param service Instancia de {@link FormulaMedicaService}.
     */
    public FormulaMedicaApiController(FormulaMedicaService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<FormulaMedica>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @Override
    public ResponseEntity<FormulaMedica> crearFormula(FormulaMedica formulaMedica) {
        return ResponseEntity.ok(service.guardarFormula(formulaMedica));
    }

    @Override
    public ResponseEntity<FormulaMedica> actualizarFormula(Long id, FormulaMedica formulaMedica) {
        return ResponseEntity.ok(service.actualizarFormula(id, formulaMedica));
    }
}
