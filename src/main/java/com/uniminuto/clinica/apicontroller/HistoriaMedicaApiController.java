package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaMedicaApi;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    @Autowired
    private HistoriaMedicaService historiaMedicaService;

    @Override
    public ResponseEntity<List<HistoriaMedica>> getHistoriasMedicas() throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.listarHistoriasMedicas());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.guardarHistoriaMedica(historiaMedicaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.actualizarHistoriaMedica(historiaMedicaRq));
    }
}