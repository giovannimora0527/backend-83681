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

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST que implementa el contrato CitaApi.
 */
@RestController
public class CitaApiController implements CitaApi {

    /** Servicio de negocio de las citas. */
    @Autowired
    private CitaService citaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<Cita>> listarCitasPorRangoFechas(LocalDate fechaInicial,
                                                                LocalDate fechaFinal)
            throws BadRequestException {
        return ResponseEntity.ok(
                this.citaService.listarCitasPorRangoFechas(fechaInicial, fechaFinal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(CitaRq citaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.citaService.guardarCita(citaRq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq citaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(citaRq));
    }
}
