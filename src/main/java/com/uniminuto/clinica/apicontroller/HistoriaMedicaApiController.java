package com.uniminuto.clinica.apicontroller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.HistoriaVistaRS;
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

/**
 * Controlador REST del recurso historia clínica.
 */
@RestController
public class HistoriaMedicaApiController implements HistoriaMedicaApi {

    @Autowired
    private HistoriaMedicaService historiaMedicaService;

    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistorias() throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.listarHistorias());
    }

    @Override
    public ResponseEntity<List<HistoriaMedica>> filtrarHistorias(String fechaInicial, String fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.filtrarHistorias(fechaInicial, fechaFinal));
    }

    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistoriasPorMascota(Integer mascotaId) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.listarHistoriasPorMascota(mascotaId));
    }

    @Override
    public ResponseEntity<HistoriaMedica> consultarHistoria(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.consultarHistoria(id));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarHistoria(HistoriaMedicaRq historiaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.guardarHistoria(historiaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarHistoria(HistoriaMedicaRq historiaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.actualizarHistoria(historiaRq));
    }

    @Override
    public ResponseEntity<PaginaRS<HistoriaVistaRS>> listarHistoriasPaginadas(
            int pagina, int tamanio, String campo, String direccion,
            Long clienteId, Integer mascotaId, Long medicoId,
            LocalDate fechaDesde, LocalDate fechaHasta, String texto) throws BadRequestException {
        Filtros.Historia filtros = new Filtros.Historia(clienteId, mascotaId, medicoId, fechaDesde, fechaHasta, texto);
        return ResponseEntity.ok(this.historiaMedicaService.listarHistoriasPaginadas(filtros, pagina, tamanio, campo, direccion));
    }
}
