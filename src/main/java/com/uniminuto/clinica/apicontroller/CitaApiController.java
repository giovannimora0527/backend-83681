package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST que implementa {@link CitaApi}.
 */
@RestController
public class CitaApiController implements CitaApi {

    /**
     * Servicio de citas.
     */
    @Autowired
    private CitaService citaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Cita>> filtrarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.filtrarCitasPorFecha(fechaInicial, fechaFinal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.crearCita(citaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(citaRq));
    }
}
