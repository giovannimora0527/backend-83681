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

/** Expone las operaciones HTTP relacionadas con una anotación del historial clínico. */
@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    /** Consulta las anotaciones clínicas registradas. */
    @Override
    public ResponseEntity<List<AnotacionHistoriaRs>> listarAnotaciones() {
        return ResponseEntity.ok(anotacionHistoriaService.listarAnotaciones());
    }

    /** Consulta las anotaciones de la historia indicada. */
    @Override
    public ResponseEntity<List<AnotacionHistoriaRs>> buscarPorHistoria(Long historiaId) {
        return ResponseEntity.ok(anotacionHistoriaService.buscarPorHistoria(historiaId));
    }


    /** Registra una nueva anotación en la historia clínica. */
    @Override
    public ResponseEntity<MiRespuestaRS> crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoria) throws BadRequestException {
        return ResponseEntity.ok(anotacionHistoriaService.crearAnotacionHistoria(anotacionHistoria));
    }

    /** Guarda los cambios de una anotación clínica existente. */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoria) {
        return ResponseEntity.ok(anotacionHistoriaService.actualizarAnotacionHistoria(anotacionHistoria));
    }
}
