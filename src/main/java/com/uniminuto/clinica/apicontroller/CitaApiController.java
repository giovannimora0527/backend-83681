package com.uniminuto.clinica.apicontroller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.CitaVistaRS;
import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.entity.HorarioMedico;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.HorarioMedicoRq;
import com.uniminuto.clinica.models.HorarioMedicoRs;
import com.uniminuto.clinica.models.CerrarConsultaRq;
import com.uniminuto.clinica.models.DisponibilidadRs;
import com.uniminuto.clinica.repository.HorarioMedicoRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@RestController
public class CitaApiController implements CitaApi {

    @Autowired private HorarioMedicoRepository horarioRepository;
    @Autowired private MedicoRepository medicoRepository;

    @Autowired
    private CitaService citaService;


=======
import java.util.List;

/**
 * Controlador REST del recurso cita.
 */
@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    @Override
    public ResponseEntity<List<Cita>> listarCitas() throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitas());
    }

    @Override
<<<<<<< HEAD
    public ResponseEntity<List<Cita>> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitasRango(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<List<Cita>> listarCitasPorMascota(Integer mascotaId) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitasPorMascota(mascotaId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearCita(CitaRq cita) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.crearCita(cita));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq cita) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(cita));
    }

    @Override
    public ResponseEntity<List<HorarioMedicoRs>> listarHorarios(Long medicoId) {
        if (medicoId == null || medicoId <= 0) throw new BadRequestException("El médico es obligatorio.");
        return ResponseEntity.ok(horarioRepository.findByMedico_IdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(medicoId)
                .stream().map(this::horarioRs).toList());
    }

    @Override
    public ResponseEntity<DisponibilidadRs> consultarDisponibilidad(Long medicoId, LocalDate fecha, Integer duracionMinutos) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(medicoId, fecha, duracionMinutos));
    }

    @Override
    public ResponseEntity<HorarioMedicoRs> guardarHorario(HorarioMedicoRq request) {
        if (request == null || request.getMedicoId() == null || request.getDiaSemana() == null
                || request.getHoraInicio() == null || request.getHoraFin() == null
                || request.getDiaSemana() < 1 || request.getDiaSemana() > 7
                || !request.getHoraFin().isAfter(request.getHoraInicio())) {
            throw new BadRequestException("Indique médico, día (1-7) y un rango horario válido.");
        }
        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El médico indicado no existe."));
        if (!Boolean.TRUE.equals(medico.getEstado())) throw new BadRequestException("El médico está inactivo.");
        HorarioMedico horario = new HorarioMedico();
        horario.setMedico(medico); horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio()); horario.setHoraFin(request.getHoraFin());
        horario.setActivo(true);
        return ResponseEntity.ok(horarioRs(horarioRepository.save(horario)));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> desactivarHorario(Long id) {
        HorarioMedico horario = horarioRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El horario indicado no existe."));
        horario.setActivo(false);
        horarioRepository.save(horario);
        MiRespuestaRS response = new MiRespuestaRS();
        response.setStatus(200);
        response.setMessage("Horario desactivado correctamente.");
        return ResponseEntity.ok(response);
    }

    private HorarioMedicoRs horarioRs(HorarioMedico horario) {
        HorarioMedicoRs response = new HorarioMedicoRs();
        response.setId(horario.getId()); response.setMedicoId(horario.getMedico().getId());
        response.setDiaSemana(horario.getDiaSemana()); response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFin(horario.getHoraFin()); response.setActivo(horario.getActivo());
        return response;
    }

    @Override
    public ResponseEntity<MiRespuestaRS> cerrarConsulta(Long id, CerrarConsultaRq request) {
        return ResponseEntity.ok(citaService.cerrarConsulta(id, request));
=======
    public ResponseEntity<List<Cita>> filtrarCitas(String fechaInicial, String fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.filtrarCitas(fechaInicial, fechaFinal));
    }

    @Override
    public ResponseEntity<Cita> consultarCita(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.consultarCita(id));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.guardarCita(citaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(citaRq));
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    }

    @Override
    public ResponseEntity<MiRespuestaRS> reactivarCita(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.reactivarCita(id));
    }

    @Override
    public ResponseEntity<PaginaRS<CitaVistaRS>> listarCitasPaginadas(
            int pagina, int tamanio, String campo, String direccion,
            Long clienteId, Integer mascotaId, Long medicoId, String estado,
            LocalDate fechaDesde, LocalDate fechaHasta, String texto) throws BadRequestException {
        Filtros.Cita filtros = new Filtros.Cita(clienteId, mascotaId, medicoId, estado, fechaDesde, fechaHasta, texto);
        return ResponseEntity.ok(this.citaService.listarCitasPaginadas(filtros, pagina, tamanio, campo, direccion));
    }
}
