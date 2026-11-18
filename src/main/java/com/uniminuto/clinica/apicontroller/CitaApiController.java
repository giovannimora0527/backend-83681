package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.models.CitaMedicaRq;
import com.uniminuto.clinica.models.CitaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaReporteService;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class CitaApiController implements CitaApi {

    private final CitaService citaService;
    private final CitaReporteService reporteService;
    private final MedicoRepository medicoRepository;

    public CitaApiController(CitaService citaService, CitaReporteService reporteService,
                             MedicoRepository medicoRepository) {
        this.citaService = citaService;
        this.reporteService = reporteService;
        this.medicoRepository = medicoRepository;
    }

    @Override
    public ResponseEntity<List<CitaMedicaRs>> calendario(Long medicoId,
                                                         LocalDateTime fechaInicio,
                                                         LocalDateTime fechaFin) {
        return ResponseEntity.ok(citaService.listarCitasRango(medicoId, fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<List<CitaMedicaRs>> historial(Integer mascotaId) {
        return ResponseEntity.ok(citaService.listarCitasPorMascota(mascotaId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearCita(CitaMedicaRq cita) {
        return ResponseEntity.ok(citaService.crearCita(cita));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(Long id, CitaMedicaRq cita) {
        cita.setId(id);
        return ResponseEntity.ok(citaService.actualizarCita(cita));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> cambiarEstado(Long id, EstadoCita estado) {
        return ResponseEntity.ok(citaService.cambiarEstado(id, estado));
    }

    @Override
    public ResponseEntity<byte[]> reporteExcel(Long medicoId, LocalDateTime fechaInicio,
                                               LocalDateTime fechaFin) {
        var medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El médico no existe"));
        byte[] archivo = reporteService.exportarExcel(medico, fechaInicio, fechaFin).toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=citas-medico.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }
}
