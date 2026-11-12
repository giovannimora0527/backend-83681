package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    @Override
    public ResponseEntity<List<AnotacionHistoriaRs>> listarAnotaciones() {
        return ResponseEntity.ok(anotacionHistoriaService.listarAnotaciones());
    }

    @Override
    public ResponseEntity<List<AnotacionHistoriaRs>> buscarPorHistoria(Long historiaId) {
        return ResponseEntity.ok(anotacionHistoriaService.buscarPorHistoria(historiaId));
    }


    @Override
    public ResponseEntity<MiRespuestaRS> crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoria) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.crearAnotacionHistoria(anotacionHistoria));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoria) {
        return ResponseEntity.ok(anotacionHistoriaService.actualizarAnotacionHistoria(anotacionHistoria));
    }
}
