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
@RequestMapping("/cita/control")
public class ControlCitasApiController {
    private final JdbcTemplate db;
    public ControlCitasApiController(JdbcTemplate db) { this.db = db; }

    @GetMapping("/solapamientos")
    public List<Map<String,Object>> solapamientos(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin) {
        if (fin.isBefore(inicio) || fin.isAfter(inicio.plusMonths(3)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rango invalido (maximo 3 meses)");
        return db.queryForList("SELECT a.id cita_a,b.id cita_b,a.medico_id,a.fecha_hora inicio_a,a.fecha_fin fin_a," +
            "b.fecha_hora inicio_b,b.fecha_fin fin_b FROM cita a JOIN cita b ON a.medico_id=b.medico_id " +
            "AND a.id<b.id AND a.fecha_hora<b.fecha_fin AND a.fecha_fin>b.fecha_hora " +
            "WHERE UPPER(a.estado) NOT IN ('CANCELADA','CANCELADO') " +
            "AND UPPER(b.estado) NOT IN ('CANCELADA','CANCELADO') " +
            "AND a.fecha_hora>=? AND a.fecha_hora<? ORDER BY a.fecha_hora,a.medico_id",
            inicio.atStartOfDay(),fin.plusDays(1).atStartOfDay());
    }
}
