package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.HorarioMedicoRepository;
import com.uniminuto.clinica.entity.HorarioMedico;
import com.uniminuto.clinica.models.CerrarConsultaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.transaction.annotation.Transactional;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import com.uniminuto.clinica.models.DisponibilidadRs;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String ESTADO_PROGRAMADA = "PROGRAMADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final String ESTADO_CONFIRMADA = "CONFIRMADA";

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioMedicoRepository horarioRepository;

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;


    @Override
    public List<Cita> listarCitas() throws BadRequestException {
        return citaRepository.findAll();
    }

    @Override
    public List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        this.validarFechas(fechaInicio, fechaFin);
        List<Cita> citas = citaRepository.findByFechaHoraBetween(fechaInicio, fechaFin);
        citas.sort((cita1, cita2)
                -> cita1.getFechaHora().compareTo(cita2.getFechaHora()));
        return citas;
    }

    @Override
    public List<Cita> listarCitasPorMascota(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio y debe ser mayor a cero.");
        }
        Optional<Mascota> mascotaOptional = mascotaRepository.findById(mascotaId);
        if (mascotaOptional.isEmpty()) {
            throw new BadRequestException("La mascota no existe");
        }
        return citaRepository.findByMascotaOrderByFechaHoraDesc(mascotaOptional.get());
    }

    private void validarFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas no pueden ser nulas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio no puede ser mayor a la fecha de fin");
        }
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        this.validarCita(citaRq, false);
        Cita citaActualizar = citaRepository.findByIdForUpdate(citaRq.getId())
                .orElseThrow(() -> new BadRequestException("La cita no existe"));
        if ("ATENDIDA".equalsIgnoreCase(citaActualizar.getEstado())) {
            throw new BadRequestException("Una cita atendida no se puede modificar.");
        }

        Mascota mascota = mascotaRepository.findByIdForUpdate(citaRq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota no existe"));
        Medico medico = medicoRepository.findByIdForUpdate(citaRq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El médico no existe"));
        int duracion = duracionSolicitada(citaRq);
        String estado = citaRq.getEstado().trim().toUpperCase(java.util.Locale.ROOT);
        if (!ESTADO_CANCELADA.equals(estado)) {
            validarProfesionalDisponible(medico);
            validarSinCruces(mascota, medico, citaRq.getFechaHora(), duracion, citaActualizar.getId());
        }

        citaActualizar.setMedico(medico);
        citaActualizar.setMascota(mascota);
        citaActualizar.setFechaHora(citaRq.getFechaHora());
        citaActualizar.setMotivo(citaRq.getMotivo().trim());
        citaActualizar.setEstado(estado);
        citaActualizar.setDuracionMinutos(duracion);
        citaRepository.save(citaActualizar);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Se agendo una nueva cita para la mascota "
                + mascota.getNombreMascota() + " con el médico " + medico.getNombres() + " "
                + medico.getApellidos() + " para el día " + citaRq.getFechaHora());

        return rta;
    }

    @Override
    @Transactional
    public MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException {
        this.validarCita(citaRq, true);
        Mascota mascota = mascotaRepository.findByIdForUpdate(citaRq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota no existe"));
        Medico medico = medicoRepository.findByIdForUpdate(citaRq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El médico no existe"));
        validarProfesionalDisponible(medico);
        int duracion = duracionSolicitada(citaRq);
        validarSinCruces(mascota, medico, citaRq.getFechaHora(), duracion, null);

        Cita nuevaCita = new Cita();
        nuevaCita.setFechaHora(citaRq.getFechaHora());
        nuevaCita.setCliente(mascota.getCliente());
        nuevaCita.setMascota(mascota);
        nuevaCita.setMedico(medico);
        nuevaCita.setMotivo(citaRq.getMotivo().trim());
        nuevaCita.setEstado(ESTADO_PROGRAMADA);
        nuevaCita.setDuracionMinutos(duracion);
        citaRepository.save(nuevaCita);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Se agendo una nueva cita para la mascota "
                + mascota.getNombreMascota() + " con el médico " + medico.getNombres() + " "
                + medico.getApellidos() + " para el día " + citaRq.getFechaHora().toString());

        return rta;
    }

    private void validarProfesionalDisponible(Medico medico) {
        if (!Boolean.TRUE.equals(medico.getEstado())) {
            throw new BadRequestException("El médico está inactivo y no puede recibir citas.");
        }
    }

    private int duracionSolicitada(CitaRq request) {
        int duracion = request.getDuracionMinutos() == null ? 30 : request.getDuracionMinutos();
        if (duracion < 10 || duracion > 240 || duracion % 5 != 0) {
            throw new BadRequestException("La duración debe estar entre 10 y 240 minutos y ser múltiplo de 5.");
        }
        return duracion;
    }

    private void validarSinCruces(Mascota mascota, Medico medico, LocalDateTime inicio, int duracion, Long citaId) {
        LocalDateTime fin = inicio.plusMinutes(duracion);
        int diaSemana = inicio.getDayOfWeek().getValue();
        boolean dentroDelHorario = horarioRepository
                .findByMedico_IdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(medico.getId()).stream()
                .filter(h -> h.getDiaSemana() == diaSemana)
                .anyMatch(h -> !inicio.toLocalTime().isBefore(h.getHoraInicio())
                        && !fin.toLocalTime().isAfter(h.getHoraFin()));
        if (!dentroDelHorario) {
            throw new BadRequestException("El horario solicitado está fuera de la disponibilidad semanal del médico.");
        }
        if (!citaRepository.buscarCrucesMedico(medico.getId(), inicio, fin, citaId).isEmpty()) {
            throw new BadRequestException("El médico ya tiene una cita que se cruza con ese horario.");
        }
        if (!citaRepository.buscarCrucesMascota(mascota.getMascotaId(), inicio, fin, citaId).isEmpty()) {
            throw new BadRequestException("La mascota ya tiene una cita que se cruza con ese horario.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadRs consultarDisponibilidad(Long medicoId, LocalDate fecha, Integer duracionMinutos) {
        if (medicoId == null || medicoId <= 0 || fecha == null)
            throw new BadRequestException("Indica un médico y una fecha válidos.");
        int duracion = duracionMinutos == null ? 30 : duracionMinutos;
        if (duracion < 10 || duracion > 240 || duracion % 5 != 0)
            throw new BadRequestException("La duración debe estar entre 10 y 240 minutos y ser múltiplo de 5.");
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El médico no existe."));
        if (!Boolean.TRUE.equals(medico.getEstado())) throw new BadRequestException("El médico está inactivo.");
        int dia = fecha.getDayOfWeek().getValue();
        List<HorarioMedico> horarios = horarioRepository
                .findByMedico_IdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(medicoId).stream()
                .filter(h -> h.getDiaSemana() == dia).toList();
        List<LocalDateTime> disponibles = new ArrayList<>();
        for (HorarioMedico horario : horarios) {
            for (LocalTime hora = horario.getHoraInicio(); !hora.plusMinutes(duracion).isAfter(horario.getHoraFin()); hora = hora.plusMinutes(15)) {
                LocalDateTime inicio = LocalDateTime.of(fecha, hora);
                if (inicio.isBefore(LocalDateTime.now())) continue;
                if (citaRepository.buscarCrucesMedico(medicoId, inicio, inicio.plusMinutes(duracion), null).isEmpty())
                    disponibles.add(inicio);
            }
        }
        DisponibilidadRs response = new DisponibilidadRs(); response.setMedicoId(medicoId);
        response.setFecha(fecha); response.setDuracionMinutos(duracion);
        response.setHorariosDisponibles(disponibles.stream().distinct().sorted().toList());
        return response;
    }

    @Override
    @Transactional
    public MiRespuestaRS cerrarConsulta(Long citaId, CerrarConsultaRq request) {
        if (citaId == null || citaId <= 0 || request == null || request.getMedicoId() == null
                || request.getDescripcion() == null || request.getDescripcion().isBlank()
                || request.getDiagnostico() == null || request.getDiagnostico().isBlank()
                || request.getObservaciones() == null || request.getObservaciones().isBlank()) {
            throw new BadRequestException("La consulta requiere cita, médico, descripción, diagnóstico y observaciones.");
        }
        Cita cita = citaRepository.findByIdForUpdate(citaId)
                .orElseThrow(() -> new BadRequestException("La cita indicada no existe."));
        if ("CANCELADA".equalsIgnoreCase(cita.getEstado()) || "ANULADA".equalsIgnoreCase(cita.getEstado()))
            throw new BadRequestException("No se puede cerrar una cita cancelada.");
        if ("ATENDIDA".equalsIgnoreCase(cita.getEstado()))
            throw new BadRequestException("La cita ya fue cerrada.");
        if (!cita.getMedico().getId().equals(request.getMedicoId()))
            throw new BadRequestException("Solo el médico asignado a la cita puede cerrarla.");
        AnotacionHistoriaRq note = new AnotacionHistoriaRq(); note.setCitaId(citaId);
        note.setMedicoId(request.getMedicoId()); note.setDescripcion(request.getDescripcion().trim());
        note.setDiagnostico(request.getDiagnostico().trim()); note.setObservaciones(request.getObservaciones().trim());
        anotacionHistoriaService.crearAnotacionHistoria(note);
        cita.setEstado("ATENDIDA"); citaRepository.save(cita);
        MiRespuestaRS response = new MiRespuestaRS(); response.setStatus(200);
        response.setMessage("Consulta cerrada y anotación clínica guardada."); return response;
    }

    private void validarCita(CitaRq citaRq, boolean esNueva) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("La cita no puede ser nula");
        }
        if (!esNueva && citaRq.getId() == null) {
            throw new BadRequestException("El id de la cita es obligatorio para actualizarla.");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser nula");
        }
        String estado = citaRq.getEstado() == null ? "" : citaRq.getEstado().trim().toUpperCase(java.util.Locale.ROOT);
        boolean cancelada = ESTADO_CANCELADA.equals(estado);
        if ((esNueva || !cancelada) && citaRq.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser anterior a la fecha y hora actual");
        }
        if (!esNueva && !List.of(ESTADO_PROGRAMADA, ESTADO_CONFIRMADA, ESTADO_CANCELADA).contains(estado)) {
            throw new BadRequestException("El estado debe ser PROGRAMADA, CONFIRMADA o CANCELADA.");
        }
        if (citaRq.getMascotaId() == null) {
            throw new BadRequestException("El id de la mascota no puede ser nulo");
        }
        if (citaRq.getMedicoId() == null) {
            throw new BadRequestException("El id del médico no puede ser nulo");
        }
        if (citaRq.getMascotaId() <= 0 || citaRq.getMedicoId() <= 0) {
            throw new BadRequestException("Los identificadores de mascota y médico deben ser positivos.");
        }
        if (citaRq.getMotivo() == null || citaRq.getMotivo().isBlank()) {
            throw new BadRequestException("El motivo de la cita no puede ser nulo o vacio");
        }
    }
}
