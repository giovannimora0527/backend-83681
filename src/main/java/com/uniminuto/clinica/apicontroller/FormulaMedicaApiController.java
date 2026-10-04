package com.uniminuto.clinica.apicontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaMedicaService;

@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> listar() throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.listar());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(FormulaMedicaRq rq) throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.guardar(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(FormulaMedicaRq rq) throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.actualizar(rq));
    }
}
