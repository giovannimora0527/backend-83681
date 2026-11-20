package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.CitaHistorial;
import com.uniminuto.clinica.entity.CitaDisponibilidad;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.DisponibilidadMedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.CitaHistorialRepository;
import com.uniminuto.clinica.repository.CitaDisponibilidadRepository;
import com.uniminuto.clinica.repository.MedicoUsuarioRepository;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.DayOfWeek;
import java.time.LocalTime;

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

    @Autowired private CitaHistorialRepository historialRepository;
    @Autowired private CitaDisponibilidadRepository disponibilidadRepository;
    @Autowired private MedicoUsuarioRepository medicoUsuarioRepository;


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

    @Override public List<Cita> listarCitasMedico(Long usuarioId, LocalDateTime inicio, LocalDateTime fin) {
        validarFechas(inicio, fin);
        Long medicoId = medicoUsuarioRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BadRequestException("El usuario autenticado no está vinculado a un médico.")).getMedicoId();
        return citaRepository.findByMedicoIdAndFechaHoraBetweenOrderByFechaHoraAsc(medicoId, inicio, fin);
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
        this.validarCita(citaRq);
        Optional<Mascota> mascotaOptional = mascotaRepository.findById(citaRq.getMascotaId());
        if (mascotaOptional.isEmpty()) {
            throw new BadRequestException("La mascota no existe");
        }

        Optional<Medico> medicoOptional = medicoRepository.findById(citaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico no existe");
        }

        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita no existe");
        }

        Cita citaActualizar = optCita.get();
        String estadoNuevo = normalizarEstado(citaRq.getEstado() == null ? citaActualizar.getEstado() : citaRq.getEstado());
        if (!ESTADO_CANCELADA.equals(estadoNuevo)) {
            validarMedicoActivo(medicoOptional.get());
            LocalDateTime fin = citaRq.getFechaHora().plusMinutes(30);
            validarDisponibilidad(medicoOptional.get(), citaRq.getFechaHora(), fin);
            if (citaRepository.existeCruceMedico(medicoOptional.get().getId(), citaRq.getFechaHora(), fin, citaRq.getId()))
                throw new BadRequestException("El veterinario ya tiene una cita superpuesta.");
            if (citaRepository.existeCruceMascota(mascotaOptional.get().getMascotaId(), citaRq.getFechaHora(), fin, citaRq.getId()))
                throw new BadRequestException("La mascota ya tiene una cita superpuesta.");
        }

        citaActualizar.setMedico(medicoOptional.get());
        citaActualizar.setMascota(mascotaOptional.get());
        citaActualizar.setFechaHora(citaRq.getFechaHora());
        citaActualizar.setFechaFin(citaRq.getFechaHora().plusMinutes(30));
        citaActualizar.setMotivo(citaRq.getMotivo());
        String estadoAnterior = citaActualizar.getEstado();
        citaActualizar.setEstado(estadoNuevo);
        citaActualizar.setFechaActualizacion(LocalDateTime.now());
        citaRepository.save(citaActualizar);
        registrarCambio(citaActualizar, estadoAnterior, estadoNuevo, "Cita actualizada");

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Se agendo una nueva cita para la mascota "
                + mascotaOptional.get().getNombreMascota() + " con el médico " + medicoOptional.get().getNombres() + " "
                + medicoOptional.get().getApellidos() + " para el día " + citaRq.getFechaHora().toString());

        return rta;
    }

    @Override
    @Transactional
    public MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException {
        this.validarCita(citaRq);
        Optional<Mascota> mascotaOptional = mascotaRepository.findById(citaRq.getMascotaId());
        if (mascotaOptional.isEmpty()) {
            throw new BadRequestException("La mascota no existe");
        }

        Optional<Medico> medicoOptional = medicoRepository.findById(citaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico no existe");
        }

        Medico medico = medicoOptional.get();
        validarMedicoActivo(medico);
        LocalDateTime fin = citaRq.getFechaHora().plusMinutes(30);
        validarDisponibilidad(medico, citaRq.getFechaHora(), fin);
        if (citaRepository.existeCruceMedico(medico.getId(), citaRq.getFechaHora(), fin, null))
            throw new BadRequestException("El veterinario ya tiene una cita superpuesta.");
        if (citaRepository.existeCruceMascota(mascotaOptional.get().getMascotaId(), citaRq.getFechaHora(), fin, null))
            throw new BadRequestException("La mascota ya tiene una cita superpuesta.");

        Mascota mascota = mascotaOptional.get();
        Cita nuevaCita = new Cita();
        nuevaCita.setFechaHora(citaRq.getFechaHora());
        nuevaCita.setFechaFin(fin);
        nuevaCita.setCliente(mascota.getCliente());
        nuevaCita.setMascota(mascota);
        nuevaCita.setMedico(medico);
        nuevaCita.setMotivo(citaRq.getMotivo());
        nuevaCita.setEstado(ESTADO_PROGRAMADA);
        nuevaCita.setFechaActualizacion(LocalDateTime.now());
        nuevaCita = citaRepository.save(nuevaCita);
        registrarCambio(nuevaCita, null, ESTADO_PROGRAMADA, "Cita creada");

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Se agendo una nueva cita para la mascota "
                + mascota.getNombreMascota() + " con el médico " + medico.getNombres() + " "
                + medico.getApellidos() + " para el día " + citaRq.getFechaHora().toString());

        return rta;
    }

    private boolean sePuedeAgendarCitaXFecha(Cita cita) {
        LocalDateTime fechaHoraCita = cita.getFechaHora();
        LocalDateTime fechaHoraCitaMas30Min = fechaHoraCita.plusMinutes(15);
        LocalDateTime fechaHoraCitaMenos30Min = fechaHoraCita.minusMinutes(15);
        return !cita.getFechaHora().isAfter(fechaHoraCitaMenos30Min)
                || !cita.getFechaHora().isBefore(fechaHoraCitaMas30Min)
                || !cita.getEstado().equals(ESTADO_PROGRAMADA);
    }

    private void validarCita(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("La cita no puede ser nula");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser nula");
        }
        if (citaRq.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser anterior a la fecha y hora actual");
        }
        if (citaRq.getMascotaId() == null) {
            throw new BadRequestException("El id de la mascota no puede ser nulo");
        }
        if (citaRq.getMedicoId() == null) {
            throw new BadRequestException("El id del medico no puede ser nulo");
        }
        if (citaRq.getMotivo() == null || citaRq.getMotivo().isEmpty()) {
            throw new BadRequestException("El motivo de la cita no puede ser nulo o vacio");
        }
    }

    public CitaDisponibilidad crearDisponibilidad(DisponibilidadMedicoRq r) {
        if (r == null || r.medicoId() == null || r.diaSemana() == null || r.horaInicio() == null || r.horaFin() == null
                || r.diaSemana() < 1 || r.diaSemana() > 7 || !r.horaInicio().isBefore(r.horaFin()))
            throw new BadRequestException("Médico, día ISO (1-7) y rango horario válido son obligatorios.");
        Medico medico = medicoRepository.findById(r.medicoId()).orElseThrow(() -> new BadRequestException("El médico no existe."));
        CitaDisponibilidad d = new CitaDisponibilidad();
        d.setMedico(medico); d.setDiaSemana(r.diaSemana()); d.setHoraInicio(r.horaInicio()); d.setHoraFin(r.horaFin()); d.setActivo(true);
        return disponibilidadRepository.save(d);
    }

    public List<CitaDisponibilidad> disponibilidadMedico(Long medicoId) {
        if (medicoId == null || !medicoRepository.existsById(medicoId)) throw new BadRequestException("El médico no existe.");
        return disponibilidadRepository.findByMedicoIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(medicoId);
    }

    private void validarMedicoActivo(Medico medico) {
        if (!Boolean.TRUE.equals(medico.getEstado())) throw new BadRequestException("No se pueden asignar citas a un veterinario inactivo.");
    }

    private void validarDisponibilidad(Medico medico, LocalDateTime inicio, LocalDateTime fin) {
        int dia = inicio.getDayOfWeek().getValue();
        LocalTime horaInicio = inicio.toLocalTime();
        LocalTime horaFin = fin.toLocalTime();
        boolean disponible = disponibilidadRepository.findByMedicoIdAndDiaSemanaAndActivoTrue(medico.getId(), dia).stream()
                .anyMatch(d -> !horaInicio.isBefore(d.getHoraInicio()) && !horaFin.isAfter(d.getHoraFin()));
        if (!disponible) throw new BadRequestException("El veterinario no tiene disponibilidad registrada para ese horario.");
    }

    private void registrarCambio(Cita cita, String anterior, String nuevo, String nota) {
        CitaHistorial h = new CitaHistorial();
        h.setCitaId(cita.getId()); h.setEstadoAnterior(anterior); h.setEstadoNuevo(nuevo);
        h.setNota(nota); h.setCreadoEn(LocalDateTime.now()); historialRepository.save(h);
    }

    private String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) return ESTADO_PROGRAMADA;
        String value = estado.trim().toUpperCase();
        if (value.equals("PENDIENTE")) value = ESTADO_PROGRAMADA;
        if (!List.of(ESTADO_PROGRAMADA, ESTADO_CONFIRMADA, ESTADO_CANCELADA, "ATENDIDA").contains(value))
            throw new BadRequestException("Estado de cita no permitido.");
        return value;
    }
}
