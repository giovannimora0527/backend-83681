package com.uniminuto.clinica.modulos.citas;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.security.AuthUser;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.security.Roles;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import com.uniminuto.clinica.support.PdfKit;
import com.uniminuto.clinica.support.XlsxKit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Módulo 2 - Citas y calendario. Concentra las reglas de agenda:
 * médico activo, horario disponible, sin traslapes del veterinario ni de la mascota,
 * cancelaciones con historial. Lo usan tanto el calendario nuevo del médico como el módulo
 * de citas ya existente (secretaria/administrador).
 */
@Service
public class CitasService {

    public static final int DURACION_DEFAULT = 30;

    private static final String SELECT_CITA = """
            SELECT c.id, c.cliente_id, c.mascota_id, c.medico_id, c.fecha_hora AS inicio, c.fecha_hora_fin AS fin,
                   c.duracion_min, c.estado, e.nombre AS estado_nombre, e.color, c.motivo, c.motivo_cancelacion,
                   c.fecha_cancelacion, ma.nombre_mascota, ma.edad AS mascota_edad, r.nombre AS raza, r.especie,
                   CONCAT(cl.nombres, ' ', cl.apellidos) AS propietario, cl.telefono AS propietario_telefono,
                   cl.numero_documento AS propietario_documento,
                   CONCAT(me.nombres, ' ', me.apellidos) AS medico_nombre, es.nombre AS especialidad
              FROM cita c
              JOIN estado_cita e ON e.codigo = c.estado
              JOIN mascota ma ON ma.mascota_id = c.mascota_id
              JOIN raza r ON r.raza_id = ma.raza_id
              JOIN cliente cl ON cl.cliente_id = c.cliente_id
              JOIN medico me ON me.id = c.medico_id
              JOIN especializacion es ON es.id = me.especializacion_id
            """;

    private final Db db;
    private final CurrentUser current;

    public CitasService(Db db, CurrentUser current) {
        this.db = db;
        this.current = current;
    }

    // ------------------------------------------------------------------ consultas

    /** Veterinario asociado al usuario autenticado (para el calendario del médico). */
    public Map<String, Object> miMedico() {
        Long id = current.medicoId();
        if (id == null) {
            throw new BadRequestException("Su usuario no está asociado a un médico veterinario.", HttpStatus.FORBIDDEN);
        }
        return db.must("El médico no existe.", """
                SELECT m.id, m.nombres, m.apellidos, m.registro_profesional, m.estado, es.nombre AS especialidad
                  FROM medico m JOIN especializacion es ON es.id = m.especializacion_id WHERE m.id = ?
                """, id);
    }

    /** Médico sobre el que se consulta: el propio si el usuario es MEDICO; el indicado si es ADMIN. */
    private long medicoConsulta(Long medicoIdParam) {
        AuthUser u = current.get();
        if (u.is(Roles.MEDICO)) {
            Long propio = current.medicoId();
            if (propio == null) {
                throw new BadRequestException("Su usuario no está asociado a un médico veterinario.", HttpStatus.FORBIDDEN);
            }
            return propio;
        }
        if (medicoIdParam == null) {
            throw new BadRequestException("Indique el médico a consultar (medicoId).");
        }
        return medicoIdParam;
    }

    /** Eventos del calendario del veterinario en un rango de fechas (vista día/semana/mes). */
    public List<Map<String, Object>> calendario(LocalDate inicio, LocalDate fin, Long medicoIdParam) {
        long medicoId = medicoConsulta(medicoIdParam);
        return db.list(SELECT_CITA + " WHERE c.medico_id = ? AND c.fecha_hora < ? AND c.fecha_hora_fin > ? ORDER BY c.fecha_hora",
                medicoId, fin.plusDays(1).atStartOfDay(), inicio.atStartOfDay());
    }

    public Map<String, Object> obtener(long id) {
        Map<String, Object> c = db.must("La cita no existe.", SELECT_CITA + " WHERE c.id = ?", id);
        verificarPropiedad(c);
        return c;
    }

    /** Historial de citas de una mascota (incluye canceladas). */
    /** Veterinarios activos para que el administrador consulte su calendario. */
    public List<Map<String, Object>> medicosParaAgenda() {
        return db.list("SELECT m.id, m.nombres, m.apellidos, m.registro_profesional, es.nombre AS especialidad "
                + "FROM medico m JOIN especializacion es ON es.id = m.especializacion_id "
                + "WHERE m.estado = 1 ORDER BY m.apellidos, m.nombres");
    }

    /** Mascotas activas (con su propietario) para agendar citas desde el calendario. */
    public List<Map<String, Object>> mascotasParaAgenda() {
        return db.list("SELECT m.mascota_id AS id, m.nombre_mascota AS nombre, r.nombre AS raza, r.especie, "
                + "CONCAT(c.nombres, ' ', c.apellidos) AS propietario "
                + "FROM mascota m JOIN raza r ON r.raza_id = m.raza_id JOIN cliente c ON c.cliente_id = m.cliente_id "
                + "WHERE m.activo = 1 AND c.activo = 1 ORDER BY m.nombre_mascota");
    }

    public List<Map<String, Object>> porMascota(int mascotaId) {
        db.must("La mascota no existe.", "SELECT mascota_id FROM mascota WHERE mascota_id = ?", mascotaId);
        return db.list(SELECT_CITA + " WHERE c.mascota_id = ? ORDER BY c.fecha_hora DESC", mascotaId);
    }

    public List<Map<String, Object>> horarios(Long medicoIdParam) {
        long medicoId = medicoConsulta(medicoIdParam);
        return horariosDe(medicoId);
    }

    public List<Map<String, Object>> horariosDe(long medicoId) {
        return db.list("SELECT id, dia_semana, hora_inicio, hora_fin, activo FROM medico_horario "
                + "WHERE medico_id = ? ORDER BY dia_semana, hora_inicio", medicoId);
    }

    /** Reemplaza el horario de disponibilidad del veterinario (administración). */
    @Transactional
    public List<Map<String, Object>> guardarHorarios(long medicoId, List<Map<String, Object>> bloques) {
        db.must("El médico no existe.", "SELECT id FROM medico WHERE id = ?", medicoId);
        db.update("DELETE FROM medico_horario WHERE medico_id = ?", medicoId);
        for (Map<String, Object> b : bloques) {
            Integer dia = In.intg(b, "diaSemana");
            LocalTime ini = LocalTime.parse(In.req(b, "horaInicio", "Hora de inicio"));
            LocalTime fin = LocalTime.parse(In.req(b, "horaFin", "Hora de fin"));
            if (dia == null || dia < 1 || dia > 7) {
                throw new BadRequestException("El día de la semana debe estar entre 1 (lunes) y 7 (domingo).");
            }
            if (!fin.isAfter(ini)) {
                throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio.");
            }
            db.update("INSERT INTO medico_horario (medico_id, dia_semana, hora_inicio, hora_fin, activo) VALUES (?,?,?,?,?)",
                    medicoId, dia, ini, fin, In.bool(b, "activo", true));
        }
        return horariosDe(medicoId);
    }

    // ------------------------------------------------------------------ escritura

    /**
     * Agenda una cita nueva. Si el usuario es MEDICO, la cita se asigna a su propio perfil.
     * Campos: mascotaId, medicoId, fechaHora, duracionMin (opcional), motivo.
     */
    @Transactional
    public Map<String, Object> crear(Map<String, Object> body) {
        long mascotaId = In.reqLng(body, "mascotaId", "Mascota");
        long medicoId = current.get().is(Roles.MEDICO) ? medicoConsulta(null) : In.reqLng(body, "medicoId", "Médico");
        LocalDateTime inicio = In.reqDateTime(body, "fechaHora", "Fecha y hora");
        int duracion = In.intg(body, "duracionMin") == null ? DURACION_DEFAULT : In.intg(body, "duracionMin");
        String motivo = In.str(body, "motivo");
        long id = agendar(null, mascotaId, medicoId, inicio, duracion, motivo);
        return db.one(SELECT_CITA + " WHERE c.id = ?", id);
    }

    /** Edita o reprograma una cita PENDIENTE o CONFIRMADA aplicando de nuevo todas las reglas de agenda. */
    @Transactional
    public Map<String, Object> editar(long id, Map<String, Object> body) {
        Map<String, Object> actual = db.must("La cita no existe.",
                "SELECT id, mascota_id, medico_id, fecha_hora, duracion_min, estado, motivo FROM cita WHERE id = ? FOR UPDATE", id);
        verificarPropiedadMedico(In.toLong(actual.get("medicoId")));
        String estado = String.valueOf(actual.get("estado"));
        if (!estado.equals("PENDIENTE") && !estado.equals("CONFIRMADA")) {
            throw new BadRequestException("Solo se pueden modificar citas pendientes o confirmadas (estado actual: " + estado + ").");
        }
        long mascotaId = In.lng(body, "mascotaId") != null ? In.lng(body, "mascotaId") : In.toLong(actual.get("mascotaId"));
        long medicoId = current.get().is(Roles.MEDICO) ? In.toLong(actual.get("medicoId"))
                : (In.lng(body, "medicoId") != null ? In.lng(body, "medicoId") : In.toLong(actual.get("medicoId")));
        LocalDateTime inicio = In.dateTime(body, "fechaHora") != null ? In.dateTime(body, "fechaHora")
                : LocalDateTime.parse(String.valueOf(actual.get("fechaHora")));
        int duracion = In.intg(body, "duracionMin") != null ? In.intg(body, "duracionMin") : In.toInt(actual.get("duracionMin"));
        String motivo = body.containsKey("motivo") ? In.str(body, "motivo") : (String) actual.get("motivo");
        boolean cambioFecha = !inicio.equals(LocalDateTime.parse(String.valueOf(actual.get("fechaHora"))))
                || duracion != In.toInt(actual.get("duracionMin")) || medicoId != In.toLong(actual.get("medicoId"));
        reprogramarSql(id, mascotaId, medicoId, inicio, duracion, motivo, cambioFecha);
        return db.one(SELECT_CITA + " WHERE c.id = ?", id);
    }

    private void reprogramarSql(long id, long mascotaId, long medicoId, LocalDateTime inicio, int duracion,
                                String motivo, boolean cambioFecha) {
        Map<String, Object> mascota = validarAgenda(id, mascotaId, medicoId, inicio, duracion, cambioFecha);
        db.update("""
                UPDATE cita SET cliente_id = ?, mascota_id = ?, medico_id = ?, fecha_hora = ?, fecha_hora_fin = ?,
                                duracion_min = ?, motivo = ?, fecha_modificacion = NOW()
                 WHERE id = ?
                """, In.toLong(mascota.get("clienteId")), mascotaId, medicoId, inicio, inicio.plusMinutes(duracion),
                duracion, motivo, id);
    }

    /**
     * Crea la cita validando las reglas de negocio. Debe invocarse dentro de una transacción
     * (bloquea la fila del médico para que dos agendamientos simultáneos no se traslapen).
     */
    public long agendar(Long idExcluir, long mascotaId, long medicoId, LocalDateTime inicio, int duracion, String motivo) {
        Map<String, Object> mascota = validarAgenda(idExcluir, mascotaId, medicoId, inicio, duracion, true);
        return db.insert("""
                INSERT INTO cita (cliente_id, mascota_id, medico_id, fecha_hora, fecha_hora_fin, duracion_min, estado, motivo)
                VALUES (?,?,?,?,?,?, 'PENDIENTE', ?)
                """, In.toLong(mascota.get("clienteId")), mascotaId, medicoId, inicio, inicio.plusMinutes(duracion),
                duracion, motivo);
    }

    /** Reglas de negocio de agenda. Devuelve la mascota (con su propietario) para completar la cita. */
    private Map<String, Object> validarAgenda(Long idExcluir, long mascotaId, long medicoId, LocalDateTime inicio,
                                              int duracion, boolean validarPasado) {
        if (duracion < 10 || duracion > 240) {
            throw new BadRequestException("La duración de la cita debe estar entre 10 y 240 minutos.");
        }
        LocalDateTime fin = inicio.plusMinutes(duracion);
        if (!fin.toLocalDate().equals(inicio.toLocalDate())) {
            throw new BadRequestException("La cita debe iniciar y terminar el mismo día.");
        }
        if (validarPasado && inicio.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("No se pueden programar citas en una fecha/hora que ya pasó.");
        }
        // Cada cita pertenece a una mascota, un propietario y un médico
        Map<String, Object> mascota = db.must("La mascota no existe.",
                "SELECT mascota_id, cliente_id, activo, nombre_mascota FROM mascota WHERE mascota_id = ?", mascotaId);
        if (!Boolean.TRUE.equals(mascota.get("activo"))) {
            throw new BadRequestException("La mascota está inactiva y no se le pueden asignar citas.");
        }
        // Bloquea al médico mientras se valida y agenda (evita condiciones de carrera)
        Map<String, Object> medico = db.must("El médico no existe.",
                "SELECT id, estado FROM medico WHERE id = ? FOR UPDATE", medicoId);
        if (!Boolean.TRUE.equals(medico.get("estado"))) {
            throw new BadRequestException("No se pueden asignar citas a médicos inactivos.");
        }
        long disponible = db.count("""
                SELECT COUNT(*) FROM medico_horario
                 WHERE medico_id = ? AND dia_semana = ? AND activo = 1 AND hora_inicio <= ? AND hora_fin >= ?
                """, medicoId, inicio.getDayOfWeek().getValue(), inicio.toLocalTime(), fin.toLocalTime());
        if (disponible == 0) {
            throw new BadRequestException("El médico no tiene disponibilidad en ese horario (" + inicio.toLocalDate()
                    + " " + inicio.toLocalTime() + " - " + fin.toLocalTime() + ").");
        }
        long excluir = idExcluir == null ? -1L : idExcluir;
        Map<String, Object> choqueMedico = db.one("""
                SELECT fecha_hora, fecha_hora_fin FROM cita
                 WHERE medico_id = ? AND estado <> 'CANCELADA' AND id <> ? AND fecha_hora < ? AND fecha_hora_fin > ? LIMIT 1
                """, medicoId, excluir, fin, inicio);
        if (choqueMedico != null) {
            throw new BadRequestException("El médico ya tiene una cita en ese horario (" + choqueMedico.get("fechaHora")
                    + " a " + choqueMedico.get("fechaHoraFin") + ").");
        }
        Map<String, Object> choqueMascota = db.one("""
                SELECT fecha_hora, fecha_hora_fin FROM cita
                 WHERE mascota_id = ? AND estado <> 'CANCELADA' AND id <> ? AND fecha_hora < ? AND fecha_hora_fin > ? LIMIT 1
                """, mascotaId, excluir, fin, inicio);
        if (choqueMascota != null) {
            throw new BadRequestException("La mascota " + mascota.get("nombreMascota") + " ya tiene otra cita en ese horario ("
                    + choqueMascota.get("fechaHora") + " a " + choqueMascota.get("fechaHoraFin") + ").");
        }
        return mascota;
    }

    @Transactional
    public Map<String, Object> confirmar(long id) {
        Map<String, Object> c = bloquear(id);
        if (!"PENDIENTE".equals(c.get("estado"))) {
            throw new BadRequestException("Solo se pueden confirmar citas pendientes.");
        }
        db.update("UPDATE cita SET estado = 'CONFIRMADA', fecha_modificacion = NOW() WHERE id = ?", id);
        return db.one(SELECT_CITA + " WHERE c.id = ?", id);
    }

    /** Cancela conservando el registro (historial): guarda motivo, fecha y usuario. */
    @Transactional
    public Map<String, Object> cancelar(long id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("Debe indicar el motivo de la cancelación.");
        }
        Map<String, Object> c = bloquear(id);
        String estado = String.valueOf(c.get("estado"));
        if (!estado.equals("PENDIENTE") && !estado.equals("CONFIRMADA")) {
            throw new BadRequestException("No se puede cancelar una cita en estado " + estado + ".");
        }
        db.update("""
                UPDATE cita SET estado = 'CANCELADA', motivo_cancelacion = ?, fecha_cancelacion = NOW(),
                                usuario_cancela_id = ?, fecha_modificacion = NOW()
                 WHERE id = ?
                """, motivo.trim(), current.get().id(), id);
        return db.one(SELECT_CITA + " WHERE c.id = ?", id);
    }

    /**
     * Marca una cita como atendida. Si el veterinario ya inició la atención médica, debe finalizarla
     * desde ese módulo; si no existe atención, se registra una atención mínima para poder facturarla.
     */
    @Transactional
    public Map<String, Object> marcarAtendida(long id) {
        Map<String, Object> c = bloquear(id);
        String estado = String.valueOf(c.get("estado"));
        if (!estado.equals("PENDIENTE") && !estado.equals("CONFIRMADA")) {
            throw new BadRequestException("Solo se puede marcar como atendida una cita pendiente o confirmada. "
                    + "Si ya está en atención, finalícela desde el módulo de atención médica.");
        }
        Long medicoId = In.toLong(c.get("medicoId"));
        long historiaId = asegurarHistoria(In.toInt(c.get("mascotaId")));
        db.insert("""
                INSERT INTO atencion_medica (cita_id, historia_id, medico_id, mascota_id, motivo_consulta, estado, fecha_fin)
                VALUES (?,?,?,?,?, 'FINALIZADA', NOW())
                """, id, historiaId, medicoId, In.toInt(c.get("mascotaId")),
                c.get("motivo") == null ? "Consulta veterinaria" : String.valueOf(c.get("motivo")));
        db.update("UPDATE cita SET estado = 'ATENDIDA', fecha_modificacion = NOW() WHERE id = ?", id);
        return db.one(SELECT_CITA + " WHERE c.id = ?", id);
    }

    /** Historia clínica única por mascota (se crea si no existe). */
    public long asegurarHistoria(int mascotaId) {
        db.update("INSERT IGNORE INTO historia_medica (paciente_id) VALUES (?)", mascotaId);
        Long h = db.scalar(Long.class, "SELECT id FROM historia_medica WHERE paciente_id = ?", mascotaId);
        if (h == null) {
            throw new BadRequestException("No fue posible obtener la historia clínica de la mascota.");
        }
        return h;
    }

    // ------------------------------------------------------------------ reporte Excel

    /** Excel con las citas del médico (propio si es MEDICO) en un rango de fechas. */
    public byte[] reporteExcel(LocalDate inicio, LocalDate fin, Long medicoIdParam) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new BadRequestException("Indique un rango de fechas válido (inicio y fin).");
        }
        long medicoId = medicoConsulta(medicoIdParam);
        Map<String, Object> medico = db.must("El médico no existe.",
                "SELECT CONCAT(nombres,' ',apellidos) AS nombre FROM medico WHERE id = ?", medicoId);
        List<Map<String, Object>> citas = calendario(inicio, fin, medicoId);
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> c : citas) {
            String ini = String.valueOf(c.get("inicio"));
            String fi = String.valueOf(c.get("fin"));
            rows.add(java.util.Arrays.asList(c.get("id"), ini.substring(0, 10), ini.substring(11), fi.substring(11),
                    c.get("estadoNombre"), c.get("nombreMascota"), c.get("propietario"), c.get("propietarioTelefono"),
                    c.get("motivo"), c.get("especialidad")));
        }
        List<Object> total = java.util.Arrays.asList("TOTAL", citas.size() + " citas", null, null, null, null, null, null, null, null);
        return XlsxKit.sheet("Citas", "Citas del médico " + medico.get("nombre"),
                "Rango: " + inicio + " a " + fin + " | Generado: " + PdfKit.now(),
                List.of("ID", "Fecha", "Hora inicio", "Hora fin", "Estado", "Mascota", "Propietario", "Teléfono",
                        "Motivo", "Especialidad"), rows, total);
    }

    // ------------------------------------------------------------------ utilidades

    private Map<String, Object> bloquear(long id) {
        Map<String, Object> c = db.must("La cita no existe.",
                "SELECT id, mascota_id, medico_id, estado, motivo FROM cita WHERE id = ? FOR UPDATE", id);
        verificarPropiedadMedico(In.toLong(c.get("medicoId")));
        return c;
    }

    private void verificarPropiedad(Map<String, Object> cita) {
        verificarPropiedadMedico(In.toLong(cita.get("medicoId")));
    }

    /** Un MEDICO solo puede operar sobre sus propias citas. */
    private void verificarPropiedadMedico(long medicoIdDeLaCita) {
        if (current.get().is(Roles.MEDICO)) {
            Long propio = current.medicoId();
            if (propio == null || propio != medicoIdDeLaCita) {
                throw new BadRequestException("La cita pertenece a otro veterinario.", HttpStatus.FORBIDDEN);
            }
        }
    }
}
