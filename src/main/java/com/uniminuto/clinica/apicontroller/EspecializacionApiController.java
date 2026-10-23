package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EspecializacionApiController implements EspecializacionApi {

    private final EspecializacionService especializacionService;

    public EspecializacionApiController(EspecializacionService especializacionService) {
        this.especializacionService = especializacionService;
    }

    @Override
    public ResponseEntity<List<Especializacion>> listarEspecializaciones() {
        return ResponseEntity.ok(especializacionService.listarEspecializaciones());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarEspecializacion(EspecializacionRq rq) {
        return ResponseEntity.status(201).body(especializacionService.crearEspecializacion(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarEspecializacion(EspecializacionRq rq) {
        return ResponseEntity.ok(especializacionService.actualizarEspecializacion(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> eliminarEspecializacion(Long id) {
        return ResponseEntity.ok(especializacionService.eliminarEspecializacion(id));
    }
}
