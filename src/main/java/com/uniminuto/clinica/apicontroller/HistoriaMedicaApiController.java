// apicontroller/HistoriaMedicaApiController.java
package com.uniminuto.clinica.apicontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaMedicaService;

@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    @Autowired
    private HistoriaMedicaService historiaMedicaService;

    @Override
    public ResponseEntity<List<HistoriaMedica>> listar() throws BadRequestException {
        return ResponseEntity.ok(historiaMedicaService.listar());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(HistoriaMedicaRq rq) throws BadRequestException {
        return ResponseEntity.ok(historiaMedicaService.guardarHistoria(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(HistoriaMedicaRq rq) throws BadRequestException {
        return ResponseEntity.ok(historiaMedicaService.actualizarHistoria(rq));
    }
}