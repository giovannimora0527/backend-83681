package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionService;

    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarAnotacionesPorHistoria(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(anotacionService.listarAnotacionesPorHistoria(historiaId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearAnotacionHistoria(AnotacionHistoriaRq request) throws BadRequestException {
        return ResponseEntity.ok(anotacionService.crearAnotacionHistoria(request));
    }

    @GetMapping("/listar-anotaciones")
    public ResponseEntity<List<AnotacionHistoria>> listarAnotaciones() throws BadRequestException {
        return ResponseEntity.ok(anotacionService.listarAnotaciones());
    }
}