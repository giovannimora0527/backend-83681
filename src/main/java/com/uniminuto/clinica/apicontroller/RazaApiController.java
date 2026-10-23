package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.RazaApi;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RazaApiController implements RazaApi {

    private final RazaService razaService;

    public RazaApiController(RazaService razaService) {
        this.razaService = razaService;
    }

    @Override
    public ResponseEntity<List<Raza>> listarRazas() {
        return ResponseEntity.ok(razaService.listarRazas());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarRaza(RazaRq rq) {
        return ResponseEntity.status(201).body(razaService.crearRaza(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarRaza(RazaRq rq) {
        return ResponseEntity.ok(razaService.actualizarRaza(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> eliminarRaza(Integer id) {
        return ResponseEntity.ok(razaService.eliminarRaza(id));
    }
}
