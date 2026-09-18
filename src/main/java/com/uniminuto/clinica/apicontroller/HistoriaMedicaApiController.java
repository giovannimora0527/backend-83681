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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST que implementa {@link HistoriaMedicaApi}.
 */
@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    /**
     * Servicio de historias médicas.
     */
    @Autowired
    private HistoriaMedicaService historiaMedicaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.crearHistoriaMedica(historiaMedicaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistoriasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.listarHistoriasPorFecha(fechaInicial, fechaFinal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.actualizarHistoriaMedica(historiaMedicaRq));
    }
}
