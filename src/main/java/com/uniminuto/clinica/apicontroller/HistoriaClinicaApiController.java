package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaClinicaApi;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaClinicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaClinicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de historias clinicas. Recibe la peticion HTTP y delega al servicio.
 */
@RestController
public class HistoriaClinicaApiController implements HistoriaClinicaApi {

    /**
     * Servicio con la logica de negocio de las historias clinicas.
     */
    @Autowired
    private HistoriaClinicaService historiaClinicaService;

    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistorias() throws BadRequestException {
        return ResponseEntity.ok(historiaClinicaService.listarHistorias());
    }

    @Override
    public ResponseEntity<HistoriaMedica> consultarHistoria(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(historiaClinicaService.consultarHistoria(historiaId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearHistoria(HistoriaClinicaRq historiaRq) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED).body(historiaClinicaService.crearHistoria(historiaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> cerrarHistoria(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(historiaClinicaService.cerrarHistoria(historiaId));
    }
}
