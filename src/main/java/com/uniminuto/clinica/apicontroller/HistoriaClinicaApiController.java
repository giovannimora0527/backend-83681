package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Recibe las solicitudes de historia clínica y delega las validaciones de acceso y edición al
 * servicio.
 */
@RestController
public class HistoriaClinicaApiController implements HistoriaApi {

    @Autowired
    private HistoriaService historiaService;

    /** Consulta todas las historias clínicas disponibles. */
    @Override
    public ResponseEntity<List<HistoriaMedicaRs>> listarAllHIstorias() throws BadRequestException {
        return ResponseEntity.ok(this.historiaService.getAllHistorias());
    }

    /** Busca las historias clínicas que coinciden con el criterio. */
    @Override
    public ResponseEntity<List<HistoriaMedicaRs>> buscarHistorias(String termino) {
        return ResponseEntity.ok(this.historiaService.buscarHistorias(termino));
    }

    /** Guarda los datos de la historia clínica. */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoria(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.historiaService.guardarHistoria(historiaMedicaRq));
    }

    /** Guarda cambios en la historia clínica del paciente. */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoria(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        return ResponseEntity.ok(this.historiaService.actualizarHistoria(historiaMedicaRq));
    }
}
