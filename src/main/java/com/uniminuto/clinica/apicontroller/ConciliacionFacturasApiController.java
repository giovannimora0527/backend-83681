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
@RequestMapping("/reportes/conciliacion")
public class ConciliacionFacturasApiController {
    private final JdbcTemplate db;
    public ConciliacionFacturasApiController(JdbcTemplate db) { this.db=db; }

    @GetMapping
    public List<Map<String,Object>> consultar(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin) {
        if(fin.isBefore(inicio) || fin.isAfter(inicio.plusYears(1)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rango invalido (maximo un anio)");
        return db.queryForList("SELECT f.id,f.numero,f.cita_id,f.fecha,f.estado,f.total," +
            "COALESCE(SUM(p.valor),0) pagado, f.total-COALESCE(SUM(p.valor),0) saldo " +
            "FROM factura f LEFT JOIN pago p ON p.factura_id=f.id " +
            "WHERE f.fecha>=? AND f.fecha<? " +
            "GROUP BY f.id,f.numero,f.cita_id,f.fecha,f.estado,f.total ORDER BY f.fecha,f.id",
            inicio.atStartOfDay(),fin.plusDays(1).atStartOfDay());
    }
}
