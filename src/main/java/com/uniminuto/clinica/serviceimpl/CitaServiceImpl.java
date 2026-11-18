package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.HorarioDisponibilidad;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaMedicaRq;
import com.uniminuto.clinica.models.CitaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HorarioDisponibilidadRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final MedicoRepository medicoRepository;
    private final HorarioDisponibilidadRepository horarioRepository;

    public CitaServiceImpl(CitaRepository citaRepository, MascotaRepository mascotaRepository,
                           MedicoRepository medicoRepository,
                           HorarioDisponibilidadRepository horarioRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaMedicaRs> listarCitasRango(Long medicoId, LocalDateTime inicio, LocalDateTime fin) {
        validarFechas(inicio, fin);
        if (medicoId == null) throw new BadRequestException("El id del médico es obligatorio");
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El médico no existe"));
        return citaRepository.findByMedicoAndRange(medico, inicio, fin.plusNanos(1))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaMedicaRs> listarCitasPorMascota(Integer mascotaId) {
        Mascota mascota = obtenerMascota(mascotaId);
        return citaRepository.findByMascotaOrderByFechaHoraInicioDesc(mascota)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public MiRespuestaRS crearCita(CitaMedicaRq rq) {
        Medico medico = obtenerMedico(rq);
        Mascota mascota = obtenerMascota(rq.getMascotaId());
        validarNuevaCita(rq, medico, mascota, null);

        Cita cita = new Cita();
        cita.setCliente(mascota.getCliente());
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHoraInicio(rq.getFechaHoraInicio());
        cita.setFechaHoraFin(rq.getFechaHoraFin());
        cita.setMotivoConsulta(rq.getMotivoConsulta().trim());
        cita.setEstado(EstadoCita.PENDIENTE);
        citaRepository.save(cita);
        return mensaje("Se creó la cita médica para " + mascota.getNombreMascota());
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarCita(CitaMedicaRq rq) {
        if (rq.getId() == null) throw new BadRequestException("El id de la cita es obligatorio");
        Cita cita = citaRepository.findById(rq.getId())
                .orElseThrow(() -> new BadRequestException("La cita no existe"));
        Medico medico = obtenerMedico(rq);
        Mascota mascota = obtenerMascota(rq.getMascotaId());
        validarNuevaCita(rq, medico, mascota, cita.getId());

        cita.setMedico(medico);
        cita.setMascota(mascota);
        cita.setCliente(mascota.getCliente());
        cita.setFechaHoraInicio(rq.getFechaHoraInicio());
        cita.setFechaHoraFin(rq.getFechaHoraFin());
        cita.setMotivoConsulta(rq.getMotivoConsulta().trim());
        citaRepository.save(cita);
        return mensaje("Se actualizó y reprogramó la cita médica");
    }

    @Override
    @Transactional
    public MiRespuestaRS cambiarEstado(Long citaId, EstadoCita estado) {
        if (citaId == null) throw new BadRequestException("El id de la cita es obligatorio");
        if (estado == null) throw new BadRequestException("El estado de la cita es obligatorio");
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new BadRequestException("La cita no existe"));
        cita.setEstado(estado);
        citaRepository.save(cita);
        return mensaje("Estado de cita actualizado a " + estado);
    }

    private void validarNuevaCita(CitaMedicaRq rq, Medico medico, Mascota mascota, Long citaId) {
        if (rq == null) throw new BadRequestException("La cita no puede ser nula");
        if (rq.getFechaHoraInicio() == null || rq.getFechaHoraFin() == null)
            throw new BadRequestException("Debe indicar fecha/hora de inicio y fin");
        if (!rq.getFechaHoraFin().isAfter(rq.getFechaHoraInicio()))
            throw new BadRequestException("La fecha/hora fin debe ser posterior a la inicial");
        if (rq.getFechaHoraInicio().isBefore(LocalDateTime.now()))
            throw new BadRequestException("No puede agendar una cita en el pasado");
        if (rq.getMotivoConsulta() == null || rq.getMotivoConsulta().isBlank())
            throw new BadRequestException("El motivo de consulta es obligatorio");
        if (medico.getActivo() == null || !medico.getActivo())
            throw new BadRequestException("El médico no se encuentra activo");

        int dia = rq.getFechaHoraInicio().getDayOfWeek().getValue();
        List<HorarioDisponibilidad> horarios = horarioRepository.findByMedicoAndDiaSemana(medico, dia);
        boolean dentroHorario = horarios.stream().anyMatch(h ->
                !rq.getFechaHoraInicio().toLocalTime().isBefore(h.getHoraInicio())
                        && !rq.getFechaHoraFin().toLocalTime().isAfter(h.getHoraFin()));
        if (!dentroHorario)
            throw new BadRequestException("La cita está fuera del horario de disponibilidad del médico");

        for (Cita existente : citaRepository.findActiveByMedico(medico)) {
            if (citaId != null && citaId.equals(existente.getId())) continue;
            if (seSolapan(rq.getFechaHoraInicio(), rq.getFechaHoraFin(),
                    existente.getFechaHoraInicio(), existente.getFechaHoraFin()))
                throw new BadRequestException("El médico ya tiene una cita en ese horario");
        }
        for (Cita existente : citaRepository.findActiveByMascota(mascota)) {
            if (citaId != null && citaId.equals(existente.getId())) continue;
            if (seSolapan(rq.getFechaHoraInicio(), rq.getFechaHoraFin(),
                    existente.getFechaHoraInicio(), existente.getFechaHoraFin()))
                throw new BadRequestException("La mascota ya tiene una cita en ese horario");
        }
    }

    private boolean seSolapan(LocalDateTime inicioA, LocalDateTime finA,
                              LocalDateTime inicioB, LocalDateTime finB) {
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }

    private Medico obtenerMedico(CitaMedicaRq rq) {
        if (rq == null || rq.getMedicoId() == null)
            throw new BadRequestException("El id del médico es obligatorio");
        return medicoRepository.findById(rq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El médico no existe"));
    }

    private Mascota obtenerMascota(Integer mascotaId) {
        if (mascotaId == null) throw new BadRequestException("El id de la mascota es obligatorio");
        return mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new BadRequestException("La mascota no existe"));
    }

    private void validarFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) throw new BadRequestException("Debe indicar el rango de fechas");
        if (inicio.isAfter(fin)) throw new BadRequestException("La fecha inicio no puede ser posterior a la fecha fin");
    }

    private CitaMedicaRs toResponse(Cita c) {
        ClienteNombre cliente = propietario(c);
        Medico medico = c.getMedico();
        return CitaMedicaRs.builder().id(c.getId())
                .propietarioId(cliente.id()).propietarioNombre(cliente.nombre())
                .mascotaId(c.getMascota().getMascotaId()).mascotaNombre(c.getMascota().getNombreMascota())
                .medicoId(medico.getId()).medicoNombre(medico.getNombres() + " " + medico.getApellidos())
                .motivoConsulta(c.getMotivoConsulta()).estado(c.getEstado())
                .fechaHoraInicio(c.getFechaHoraInicio()).fechaHoraFin(c.getFechaHoraFin()).build();
    }

    private ClienteNombre propietario(Cita c) {
        var cliente = c.getMascota().getCliente();
        return new ClienteNombre(cliente.getId(), (cliente.getNombres() + " " + cliente.getApellidos()).trim());
    }

    private record ClienteNombre(Long id, String nombre) {}

    private MiRespuestaRS mensaje(String texto) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage(texto);
        return respuesta;
    }
}
