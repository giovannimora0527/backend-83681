package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulas()
            throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.findAllFormulas());
    }

    @Override
    public ResponseEntity<FormulaMedica> buscarPorId(Long id)
            throws BadRequestException {
        FormulaMedica formula = formulaMedicaService.buscarPorId(id)
                .orElseThrow(() ->
                        new BadRequestException("La fórmula médica no existe."));

        return ResponseEntity.ok(formula);
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crear(FormulaMedicaRq solicitud)
            throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.guardar(solicitud));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(FormulaMedicaRq solicitud)
            throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.actualizar(solicitud));
    }
}