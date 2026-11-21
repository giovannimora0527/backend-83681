package com.uniminuto.clinica.apicontroller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cita/agenda")
public class AgendaMedicaApiController {
    private final JdbcTemplate db;
    public AgendaMedicaApiController(JdbcTemplate db) { this.db = db; }

    @GetMapping
    public List<Map<String, Object>> consultar(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
        @RequestParam(required = false) Long medicoId) {
        if (fin.isBefore(inicio) || inicio.plusMonths(12).isBefore(fin))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rango de fechas invalido");
        String sql = "SELECT c.id,c.cliente_id,c.mascota_id,c.medico_id,c.fecha_hora,c.fecha_fin,c.estado,c.motivo " +
            "FROM cita c WHERE c.fecha_hora >= ? AND c.fecha_hora < ?" +
            (medicoId == null ? "" : " AND c.medico_id = ?") + " ORDER BY c.fecha_hora,c.id";
        return medicoId == null
            ? db.queryForList(sql, inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay())
            : db.queryForList(sql, inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(), medicoId);
    }

    @GetMapping("/disponibilidad")
    public Map<String,Object> disponibilidad(@RequestParam Long medicoId,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<Map<String,Object>> horarios = db.queryForList(
            "SELECT hora_inicio,hora_fin FROM horario_veterinario WHERE medico_id=? AND dia_semana=? ORDER BY hora_inicio",
            medicoId, fecha.getDayOfWeek().getValue());
        List<Map<String,Object>> ocupadas = db.queryForList(
            "SELECT id,fecha_hora,fecha_fin,estado FROM cita WHERE medico_id=? AND fecha_hora < ? AND fecha_fin > ? " +
            "AND UPPER(estado) NOT IN ('CANCELADA','CANCELADO') ORDER BY fecha_hora",
            medicoId, fecha.plusDays(1).atStartOfDay(), fecha.atStartOfDay());
        return Map.of("medicoId",medicoId,"fecha",fecha.toString(),"horarios",horarios,"citasOcupadas",ocupadas);
    }
}
