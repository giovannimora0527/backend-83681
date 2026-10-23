package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRequest;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador de citas. Solo recibe la peticion HTTP y delega al servicio;
 * la logica de negocio esta en CitaServiceImpl.
 */
@RestController
public class CitaApiController implements CitaApi {

    /**
     * Servicio con la logica de negocio de las citas.
     */
    @Autowired
    private CitaService citaService;

    @Override
    public ResponseEntity<List<CitaMedica>> listarCitas() throws BadRequestException {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @Override
    public ResponseEntity<CitaMedica> consultarCita(Long citaId) throws BadRequestException {
        return ResponseEntity.ok(citaService.consultarCita(citaId));
    }

    @Override
    public ResponseEntity<List<CitaMedica>> listarCitasPorMascota(Long mascotaId) throws BadRequestException {
        return ResponseEntity.ok(citaService.listarCitasPorMascota(mascotaId));
    }

    @Override
    public ResponseEntity<List<CitaMedica>> filtrarCitasPorFechas(LocalDateTime fechaInicio,
                                                                 LocalDateTime fechaFinal)
            throws BadRequestException {
        return ResponseEntity.ok(citaService.filtrarCitasPorFecha(fechaInicio, fechaFinal));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearCita(CitaRequest citaRq) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crearCita(citaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRequest citaRq) throws BadRequestException {
        return ResponseEntity.ok(citaService.actualizarCita(citaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> reactivarCita(Long citaId) throws BadRequestException {
        return ResponseEntity.ok(citaService.reactivarCita(citaId));
    }
}
