package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.RazaApi;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RazaApiController implements RazaApi {

    private final RazaService service;

    public RazaApiController(RazaService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Raza>> listarRazas() throws BadRequestException {
        return ResponseEntity.ok(service.listarRazas());
    }

    @Override
    public ResponseEntity<Raza> guardarRaza(Raza raza) throws BadRequestException {
        return ResponseEntity.ok(service.guardarRaza(raza));
    }

    @Override
    public ResponseEntity<Raza> actualizarRaza(Raza raza) throws BadRequestException {
        return ResponseEntity.ok(service.actualizarRaza(raza));
    }
}
