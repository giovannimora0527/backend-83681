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
@RequestMapping("/reportes/finanzas")
public class ResumenFinancieroApiController {
    private final JdbcTemplate db;
    public ResumenFinancieroApiController(JdbcTemplate db) { this.db = db; }

    @GetMapping("/resumen")
    public Map<String,Object> resumen(
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin) {
        if(fin.isBefore(inicio) || inicio.plusYears(3).isBefore(fin))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rango de fechas invalido");
        Map<String,Object> facturas = db.queryForMap(
            "SELECT COUNT(*) AS cantidad,COALESCE(SUM(subtotal),0) AS subtotal," +
            "COALESCE(SUM(iva),0) AS iva,COALESCE(SUM(total),0) AS total " +
            "FROM factura WHERE fecha>=? AND fecha<? AND estado<>'ANULADA'",
            inicio.atStartOfDay(),fin.plusDays(1).atStartOfDay());
        Map<String,Object> pagos = db.queryForMap(
            "SELECT COUNT(*) AS cantidad,COALESCE(SUM(valor),0) AS total " +
            "FROM pago WHERE fecha>=? AND fecha<?",
            inicio.atStartOfDay(),fin.plusDays(1).atStartOfDay());
        return Map.of("inicio",inicio.toString(),"fin",fin.toString(),"facturas",facturas,"pagos",pagos);
    }

    @GetMapping("/facturas")
    public List<Map<String,Object>> facturas(
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fin) {
        if(fin.isBefore(inicio) || inicio.plusYears(3).isBefore(fin))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Rango de fechas invalido");
        return db.queryForList("SELECT id,numero,cita_id,fecha,subtotal,descuento,iva,total,estado " +
            "FROM factura WHERE fecha>=? AND fecha<? ORDER BY fecha DESC,id DESC",
            inicio.atStartOfDay(),fin.plusDays(1).atStartOfDay());
    }
}
