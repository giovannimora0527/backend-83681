package com.uniminuto.clinica.apicontroller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

/** Despacho transaccional de formulas: FEFO, parciales y auditoria. */
@RestController
@RequestMapping("/despacho")
public class DespachoApiController {
    private final JdbcTemplate db;
    public DespachoApiController(JdbcTemplate db) { this.db = db; }

    public record Solicitud(Long formulaId, List<Item> items) {}
    public record Item(Long formulaMedicaId, Integer cantidad) {}

    @GetMapping("/pendientes")
    public List<Map<String,Object>> pendientes() {
        return db.queryForList("SELECT f.id, f.cita_id, f.fecha, f.estado_despacho " +
                "FROM formula f WHERE (f.estado_despacho IS NULL OR f.estado_despacho <> 'DESPACHADA') " +
                "AND EXISTS (SELECT 1 FROM formula_medica fm WHERE fm.formula_id=f.id AND " +
                "fm.cantidad_prescrita > COALESCE((SELECT SUM(dd.cantidad) FROM despacho_detalle dd WHERE dd.formula_medica_id=fm.id),0)) " +
                "ORDER BY f.fecha DESC");
    }

    @GetMapping("/formula/{id}")
    public Map<String,Object> detalle(@PathVariable Long id) {
        var formulas = db.queryForList("SELECT id, cita_id, fecha, estado_despacho FROM formula WHERE id=?", id);
        if (formulas.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formula no encontrada");
        var lineas = db.queryForList("SELECT fm.id, fm.medicamento_id, fm.cantidad_prescrita, " +
                "COALESCE((SELECT SUM(dd.cantidad) FROM despacho_detalle dd WHERE dd.formula_medica_id=fm.id),0) AS entregado " +
                "FROM formula_medica fm WHERE fm.formula_id=? ORDER BY fm.id", id);
        return Map.of("formula", formulas.get(0), "items", lineas);
    }

    @PostMapping("/confirmar")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> confirmar(@RequestBody Solicitud solicitud, HttpServletRequest request) {
        if (solicitud == null || solicitud.formulaId() == null || solicitud.items() == null || solicitud.items().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar formula e items");
        Object idUsuario = request.getAttribute("usuarioId");
        if (!(idUsuario instanceof Long)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesion invalida");
        Long usuarioId = (Long) idUsuario;
        var formulas = db.queryForList("SELECT id, estado_despacho FROM formula WHERE id=? FOR UPDATE", solicitud.formulaId());
        if (formulas.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formula no encontrada");
        String anterior = String.valueOf(formulas.get(0).get("estado_despacho"));
        if ("DESPACHADA".equals(anterior)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Formula ya despachada");

        Map<Long,Integer> pedidos = new TreeMap<>();
        for (Item item : solicitud.items()) {
            if (item == null || item.formulaMedicaId() == null || item.cantidad() == null || item.cantidad() <= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item o cantidad invalida");
            if (pedidos.putIfAbsent(item.formulaMedicaId(), item.cantidad()) != null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item repetido en la solicitud");
        }
        // Bloquear medicamentos en orden determinista para evitar descuentos concurrentes.
        Map<Long,Long> medicamentoPorLinea = new TreeMap<>();
        for (Long lineaId : pedidos.keySet()) {
            var filas = db.queryForList("SELECT medicamento_id, cantidad_prescrita FROM formula_medica " +
                    "WHERE id=? AND formula_id=?", lineaId, solicitud.formulaId());
            if (filas.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item ajeno a la formula: " + lineaId);
            long medId = ((Number) filas.get(0).get("medicamento_id")).longValue();
            int prescrito = ((Number) filas.get(0).get("cantidad_prescrita")).intValue();
            Integer entregado = db.queryForObject("SELECT COALESCE(SUM(cantidad),0) FROM despacho_detalle WHERE formula_medica_id=?", Integer.class, lineaId);
            if (pedidos.get(lineaId) > prescrito - (entregado == null ? 0 : entregado))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cantidad excede lo pendiente en item " + lineaId);
            medicamentoPorLinea.put(lineaId, medId);
        }
        for (Long medId : new TreeSet<>(medicamentoPorLinea.values())) {
            if (db.queryForList("SELECT id FROM medicamento WHERE id=? FOR UPDATE", medId).isEmpty())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Medicamento inexistente: " + medId);
        }
        // Validar stock de cada medicamento antes de escribir un movimiento.
        Map<Long,Integer> totales = new TreeMap<>();
        medicamentoPorLinea.forEach((linea,med) -> totales.merge(med, pedidos.get(linea), Integer::sum));
        for (var entry : totales.entrySet()) {
            Integer disponible = db.queryForObject("SELECT COALESCE(SUM(cantidad_disponible),0) FROM lote_medicamento " +
                    "WHERE medicamento_id=? AND fecha_vence>=CURRENT_DATE()", Integer.class, entry.getKey());
            if (disponible == null || disponible < entry.getValue())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock vigente insuficiente para medicamento " + entry.getKey());
        }
        long despachoId = insertar("INSERT INTO despacho(formula_id,usuario_id,fecha,estado) VALUES(?,?,NOW(),?)",
                solicitud.formulaId(), usuarioId, "CONFIRMADO");
        int entregadas = 0;
        for (var entry : pedidos.entrySet()) {
            long lineaId = entry.getKey(); long medId = medicamentoPorLinea.get(lineaId);
            int faltante = entry.getValue();
            var lotes = db.queryForList("SELECT id,cantidad_disponible FROM lote_medicamento " +
                    "WHERE medicamento_id=? AND fecha_vence>=CURRENT_DATE() AND cantidad_disponible>0 " +
                    "ORDER BY fecha_vence ASC,id ASC FOR UPDATE", medId);
            for (var lote : lotes) {
                if (faltante == 0) break;
                long loteId = ((Number) lote.get("id")).longValue();
                int cantidad = Math.min(faltante, ((Number) lote.get("cantidad_disponible")).intValue());
                int actualizado = db.update("UPDATE lote_medicamento SET cantidad_disponible=cantidad_disponible-? " +
                        "WHERE id=? AND cantidad_disponible>=?", cantidad, loteId, cantidad);
                if (actualizado != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock modificado por otra operacion");
                int stock = db.update("UPDATE medicamento SET stock_actual=stock_actual-? WHERE id=? AND stock_actual>=?",
                        cantidad, medId, cantidad);
                if (stock != 1) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock global insuficiente o desincronizado");
                long movimiento = insertar("INSERT INTO movimiento_inventario(fecha,medicamento_id,lote_id,tipo,cantidad,usuario_id,formula_id,despacho_id,observacion) " +
                        "VALUES(NOW(),?,?, 'SALIDA',?,?,?,?,?)", medId, loteId, cantidad, usuarioId, solicitud.formulaId(), despachoId, "Despacho confirmado");
                insertar("INSERT INTO despacho_detalle(despacho_id,formula_medica_id,lote_id,cantidad,movimiento_id) VALUES(?,?,?,?,?)",
                        despachoId, lineaId, loteId, cantidad, movimiento);
                faltante -= cantidad; entregadas += cantidad;
            }
            if (faltante != 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente durante el despacho");
        }
        Integer pendientes = db.queryForObject("SELECT COUNT(*) FROM formula_medica fm WHERE fm.formula_id=? AND " +
                        "fm.cantidad_prescrita > COALESCE((SELECT SUM(dd.cantidad) FROM despacho_detalle dd WHERE dd.formula_medica_id=fm.id),0)",
                Integer.class, solicitud.formulaId());
        String nuevo = (pendientes != null && pendientes == 0) ? "DESPACHADA" : "PARCIAL";
        db.update("UPDATE formula SET estado_despacho=? WHERE id=?", nuevo, solicitud.formulaId());
        db.update("INSERT INTO despacho_estado_log(formula_id,estado_anterior,estado_nuevo,usuario_id,fecha) VALUES(?,?,?,?,NOW())",
                solicitud.formulaId(), anterior, nuevo, usuarioId);
        return Map.of("despachoId", despachoId, "formulaId", solicitud.formulaId(), "unidadesEntregadas", entregadas, "estado", nuevo);
    }

    private long insertar(String sql, Object... args) {
        KeyHolder holder = new GeneratedKeyHolder();
        db.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            return ps;
        }, holder);
        if (holder.getKey() == null) throw new IllegalStateException("No se obtuvo el identificador generado");
        return holder.getKey().longValue();
    }
}
