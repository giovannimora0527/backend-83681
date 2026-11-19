package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaCalendarioRs;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Aplica las reglas de agenda. Comprueba disponibilidad del veterinario y evita cruces del médico
 * o la mascota.
 */
@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private static final Set<String> ESTADOS = Set.of(
            "PROGRAMADA", "CONFIRMADA", "ATENDIDA", "CANCELADA", "NO_ASISTIDA");
    private static final Set<String> CERRADOS = Set.of("ATENDIDA", "CANCELADA", "NO_ASISTIDA");
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final CitaRepository citaRepository;
    private final FormulaMedicaRepository formulaRepository;
    private final MascotaRepository mascotaRepository;
    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbc;

    /** Consulta las citas. */
    @Override
    public List<Cita> listarCitas() {
        Long medico = medicoAutenticado();
        return medico == null ? citaRepository.findAll()
                : citaRepository.findByMedicoIdOrderByFechaHoraAsc(medico);
    }

    /** Consulta las citas entre la fecha inicial y la final. */
    @Override
    public List<Cita> listarCitasRango(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || !inicio.isBefore(fin)) {
            throw new BadRequestException("El inicio debe ser anterior al fin.");
        }
        return citaRepository.listarIntervalo(medicoAutenticado(), inicio, fin);
    }

    /** Consulta el historial de citas de la mascota indicada. */
    @Override
    public List<Cita> listarCitasPorMascota(Long mascotaId) {
        var mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new BadRequestException("La mascota no existe."));
        Long medico = medicoAutenticado();
        return citaRepository.findByMascotaOrderByFechaHoraDesc(mascota).stream()
                .filter(cita -> medico == null || medico.equals(cita.getMedico().getId()))
                .toList();
    }

    /** Consulta las citas del veterinario en las fechas indicadas. */
    @Override
    public List<CitaCalendarioRs> calendario(LocalDateTime inicio, LocalDateTime fin) {
        if (medicoAutenticado() == null) {
            throw new AccessDeniedException("El calendario es para el perfil veterinario.");
        }
        return listarCitasRango(inicio, fin).stream().map(CitaServiceImpl::evento).toList();
    }

    /** Consulta los datos de la cita. */
    @Override
    public CitaCalendarioRs detalle(Long id) {
        Cita cita = buscarCita(id);
        validarAcceso(cita.getMedico().getId());
        return evento(cita);
    }

    /** Registra la cita. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MiRespuestaRS crearCita(CitaRq solicitud) {
        if (solicitud == null) {
            throw new BadRequestException("Los datos de la cita son obligatorios.");
        }
        if (solicitud.getMedicoId() == null) {
            solicitud.setMedicoId(medicoAutenticado());
        }
        if (solicitud.getMedicoId() != null) {
            validarAcceso(solicitud.getMedicoId());
        }
        /**
         * Protege los cambios de agenda cuando dos solicitudes intentan usar al mismo médico o
         * paciente.
         */
        bloquearRecursos(null, solicitud.getMedicoId(), solicitud.getMascotaId());
        /**
         * Comprueba el horario y los cruces. Al agendar o editar toma como propietario al cliente
         * registrado para la mascota.
         */
        agendar(new Cita(), solicitud, "PROGRAMADA", false);
        return respuesta("Se guardó la cita.");
    }

    /** Actualiza la cita. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MiRespuestaRS actualizarCita(CitaRq solicitud) {
        if (solicitud == null || solicitud.getId() == null) {
            throw new BadRequestException("El identificador de la cita es obligatorio.");
        }
        String estadoSolicitado = solicitud.getEstado();
        if ("ATENDIDA".equals(estadoSolicitado)) {
            throw new BadRequestException("Para marcar la cita como atendida, finaliza la atención clínica.");
        }
        boolean cierre = estadoSolicitado != null && CERRADOS.contains(estadoSolicitado);
        Cita cita = bloquearCitaAbierta(solicitud.getId(), cierre ? null : solicitud.getMedicoId(),
                cierre ? null : solicitud.getMascotaId());

        String estado = solicitud.getEstado() == null ? cita.getEstado() : solicitud.getEstado();
        if (!ESTADOS.contains(estado)) {
            throw new BadRequestException("El estado de la cita no es válido.");
        }
        if (CERRADOS.contains(estado)) {
            if ("CANCELADA".equals(estado)) {
                String motivo = solicitud.getMotivoCancelacion();
                if (motivo == null || motivo.isBlank()) {
                    throw new BadRequestException("El motivo de cancelación es obligatorio.");
                }
                cita.setMotivoCancelacion(motivo.strip());
                cita.setFechaCancelacion(LocalDateTime.now(ZONA));
                cita.setUsuarioCancelacion(usuarioRepository.getReferenceById(usuarioId()));
            }
            cita.setEstado(estado);
            return respuesta("Se actualizó el estado de la cita.");
        }
        if (solicitud.getMedicoId() != null && solicitud.getMascotaId() != null
                && (!cita.getMedico().getId().equals(solicitud.getMedicoId())
                    || !cita.getMascota().getMascotaId().equals(solicitud.getMascotaId()))
                && formulaRepository.existsByCita_Id(cita.getId())) {
            throw new BadRequestException("La cita tiene fórmulas emitidas y debe conservar su médico y su mascota.");
        }
        agendar(cita, solicitud, estado, false);
        return respuesta("Se guardó la cita.");
    }

    /** Solicita cambiar el horario de la cita indicada. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CitaCalendarioRs reprogramar(Long id, LocalDateTime fechaHora) {
        if (fechaHora == null) {
            throw new BadRequestException("La nueva fecha y hora son obligatorias.");
        }
        Cita cita = bloquearCitaAbierta(id, null, null);
        var solicitud = new CitaRq();
        solicitud.setMedicoId(cita.getMedico().getId());
        solicitud.setMascotaId(cita.getMascota().getMascotaId());
        solicitud.setFechaHora(fechaHora);
        solicitud.setDuracionMinutos(cita.getDuracionMinutos());
        agendar(cita, solicitud, cita.getEstado(), true);
        return evento(cita);
    }

    /** Bloquea la cita y comprueba que todavía se pueda atender. */
    private Cita bloquearCitaAbierta(Long id, Long medicoDestino, Long mascotaDestino) {
        var asignacion = jdbc.query("SELECT medico_id, mascota_id FROM cita WHERE id = ?",
                (rs, fila) -> new AsignacionCita(rs.getLong("medico_id"), rs.getLong("mascota_id")), id)
                .stream().findFirst().orElseThrow(() -> new BadRequestException("La cita no existe."));
        validarAcceso(asignacion.medicoId());
        if (medicoDestino != null) {
            validarAcceso(medicoDestino);
        }
        bloquearRecursos(asignacion, medicoDestino, mascotaDestino);
        if (jdbc.queryForList("SELECT id FROM cita WHERE id = ? FOR UPDATE", Long.class, id).isEmpty()) {
            throw new BadRequestException("La cita no existe.");
        }
        Cita cita = buscarCita(id);
        validarAcceso(cita.getMedico().getId());
        if (!asignacion.medicoId().equals(cita.getMedico().getId())
                || !asignacion.mascotaId().equals(cita.getMascota().getMascotaId())) {
            throw new BadRequestException("La asignación de la cita cambió. Vuelve a consultar la cita.");
        }
        if (CERRADOS.contains(cita.getEstado())) {
            throw new BadRequestException("La cita ya está cerrada.");
        }
        return cita;
    }

    /** Bloquea los registros que intervienen en el cambio de la cita. */
    private void bloquearRecursos(AsignacionCita origen, Long medicoDestino, Long mascotaDestino) {
        // La agenda siempre bloquea médicos, mascotas y, por último, la cita existente.
        Stream.of(origen == null ? null : origen.medicoId(), medicoDestino)
                .filter(Objects::nonNull).distinct().sorted().forEach(id ->
                        jdbc.queryForList("SELECT id FROM medico WHERE id = ? FOR UPDATE", Long.class, id));
        Stream.of(origen == null ? null : origen.mascotaId(), mascotaDestino)
                .filter(Objects::nonNull).distinct().sorted().forEach(id ->
                        jdbc.queryForList("SELECT mascota_id FROM mascota WHERE mascota_id = ? FOR UPDATE", id));
    }

    /** Registra una cita con el horario y los datos enviados. */
    private void agendar(Cita cita, CitaRq solicitud, String estado, boolean reprogramacion) {
        if (solicitud == null || solicitud.getMedicoId() == null || solicitud.getMascotaId() == null
                || solicitud.getFechaHora() == null
                || (!reprogramacion && (solicitud.getMotivo() == null || solicitud.getMotivo().isBlank()))) {
            throw new BadRequestException("Médico, mascota, fecha y motivo son obligatorios.");
        }
        validarAcceso(solicitud.getMedicoId());
        var inicio = solicitud.getFechaHora();
        if (inicio.isBefore(LocalDateTime.now(ZONA))) {
            throw new BadRequestException("La fecha de la cita debe ser futura.");
        }

        var medico = medicoRepository.findById(solicitud.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El médico no existe."));
        if (!Boolean.TRUE.equals(medico.getEstado())) {
            throw new BadRequestException("El médico está inactivo.");
        }
        var mascota = mascotaRepository.findById(solicitud.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota no existe."));
        if (mascota.getCliente() == null) {
            throw new BadRequestException("La mascota debe tener un propietario.");
        }

        Horario horario = jdbc.query("""
                SELECT id, hora_fin, duracion_cita_min FROM horario_disponibilidad
                WHERE medico_id = ? AND dia_semana = ? AND activo = 1
                  AND hora_inicio <= ? AND hora_fin > ?
                ORDER BY hora_fin DESC, id
                """, (rs, index) -> new Horario(rs.getLong("id"),
                        rs.getObject("hora_fin", LocalTime.class), rs.getInt("duracion_cita_min")),
                medico.getId(), inicio.getDayOfWeek().getValue(), inicio.toLocalTime(), inicio.toLocalTime())
                .stream().findFirst().orElseThrow(() -> new BadRequestException("El médico no está disponible."));
        int duracion = solicitud.getDuracionMinutos() == null ? horario.duracion() : solicitud.getDuracionMinutos();
        if (duracion <= 0 || inicio.plusMinutes(duracion).isAfter(inicio.toLocalDate().atTime(horario.fin()))) {
            throw new BadRequestException("La duración debe caber en el horario disponible.");
        }
        var fin = inicio.plusMinutes(duracion);

        var cruces = jdbc.queryForList("""
                SELECT id FROM cita WHERE id <> ?
                  AND (medico_id = ? OR mascota_id = ?)
                  AND estado <> 'CANCELADA' AND fecha_hora < ?
                  AND COALESCE(hora_fin, DATE_ADD(fecha_hora, INTERVAL duracion_minutos MINUTE)) > ?
                """, Objects.requireNonNullElse(cita.getId(), 0L), medico.getId(), mascota.getMascotaId(), fin, inicio);
        if (!cruces.isEmpty()) {
            throw new BadRequestException("El médico o la mascota ya tiene una cita en ese horario.");
        }

        cita.setFechaHora(inicio);
        cita.setHoraFin(fin);
        cita.setDuracionMinutos(duracion);
        cita.setHorarioDisponibilidadId(horario.id());
        if (!reprogramacion) {
            cita.setMedico(medico);
            cita.setMascota(mascota);
            cita.setCliente(mascota.getCliente());
            cita.setMotivo(solicitud.getMotivo().trim());
            cita.setEstado(estado);
        }
        citaRepository.save(cita);
    }

    /** Busca la cita. */
    private Cita buscarCita(Long id) {
        return citaRepository.findById(id).orElseThrow(() -> new BadRequestException("La cita no existe."));
    }

    /** Comprueba los permisos del usuario para realizar la operación. */
    private void validarAcceso(Long medicoId) {
        Long medico = medicoAutenticado();
        if (medico != null && !medico.equals(medicoId)) {
            throw new AccessDeniedException("La cita corresponde a otro veterinario.");
        }
    }

    /** Identifica al veterinario vinculado con la sesión. */
    @Override
    public Long medicoAutenticado() {
        var usuario = SecurityContextHolder.getContext().getAuthentication();
        if (usuario == null) {
            throw new AccessDeniedException("Se requiere una sesión.");
        }
        boolean veterinario = usuario.getAuthorities().stream()
                .map(autoridad -> Normalizer.normalize(autoridad.getAuthority(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", ""))
                .anyMatch(Set.of("ROLE_MEDICO", "ROLE_VETERINARIO")::contains);
        if (!veterinario) {
            return null;
        }
        return jdbc.queryForList("SELECT medico_id FROM medico_usuario WHERE usuario_id = ?", Long.class, usuarioId())
                .stream().findFirst().orElseThrow(() -> new AccessDeniedException("El usuario no tiene médico asociado."));
    }

    /** Obtiene el identificador del usuario de la sesión. */
    private long usuarioId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /** Registra un cambio asociado a la operación. */
    private static CitaCalendarioRs evento(Cita cita) {
        var cliente = cita.getCliente();
        var mascota = cita.getMascota();
        var raza = mascota.getRaza();
        var fin = cita.getHoraFin() == null ? cita.getFechaHora().plusMinutes(cita.getDuracionMinutos()) : cita.getHoraFin();
        return new CitaCalendarioRs(cita.getId(), cita.getMedico().getId(), mascota.getMascotaId(),
                mascota.getNombreMascota(), cliente.getNombres() + " " + cliente.getApellidos(),
                cita.getFechaHora(), fin, cita.getMotivo(), cita.getEstado(), cliente.getId(), mascota.getEdad(),
                raza == null ? null : raza.getNombre(), raza == null ? null : raza.getEspecie());
    }

    /** Prepara el mensaje que devuelve la operación. */
    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    /** Contiene el inicio y el fin del período disponible para una cita. */
    private record Horario(long id, LocalTime fin, int duracion) {
    }

    /** Agrupa los datos de la mascota y el veterinario considerados al programar la cita. */
    private record AsignacionCita(Long medicoId, Long mascotaId) {
    }
}
