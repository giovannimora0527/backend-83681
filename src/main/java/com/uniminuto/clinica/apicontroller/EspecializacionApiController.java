package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EspecializacionApiController implements EspecializacionApi {

    @Autowired
    private EspecializacionService especializacionService;

    @Override
    public ResponseEntity<List<Especializacion>> listar() throws BadRequestException {
        return ResponseEntity.ok(especializacionService.listar());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(EspecializacionRq rq) throws BadRequestException {
        return ResponseEntity.ok(especializacionService.guardar(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(EspecializacionRq rq) throws BadRequestException {
        return ResponseEntity.ok(especializacionService.actualizar(rq));
    }
}
