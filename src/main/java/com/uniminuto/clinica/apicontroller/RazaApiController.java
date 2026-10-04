package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.RazaApi;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RazaApiController implements RazaApi {

    @Autowired
    private RazaService razaService;

    @Override
    public ResponseEntity<List<Raza>> listar() throws BadRequestException {
        return ResponseEntity.ok(razaService.listar());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(RazaRq razaRq) throws BadRequestException {
        return ResponseEntity.ok(razaService.guardar(razaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(RazaRq razaRq) throws BadRequestException {
        return ResponseEntity.ok(razaService.actualizar(razaRq));
    }
}
