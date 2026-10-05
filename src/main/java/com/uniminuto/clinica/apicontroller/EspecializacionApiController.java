package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EspecializacionApiController implements EspecializacionApi {

    private final EspecializacionService service;

    public EspecializacionApiController(EspecializacionService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Especializacion>> listarEspecializaciones() throws BadRequestException {
        return ResponseEntity.ok(service.listarEspecializaciones());
    }

    @Override
    public ResponseEntity<Especializacion> guardarEspecializacion(Especializacion especializacion)
            throws BadRequestException {
        return ResponseEntity.ok(service.guardarEspecializacion(especializacion));
    }

    @Override
    public ResponseEntity<Especializacion> actualizarEspecializacion(Especializacion especializacion)
            throws BadRequestException {
        return ResponseEntity.ok(service.actualizarEspecializacion(especializacion));
    }
}
