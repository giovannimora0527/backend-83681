package com.uniminuto.clinica.apicontroller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventario/alertas")
public class AlertasInventarioApiController {
    private final JdbcTemplate db;
    public AlertasInventarioApiController(JdbcTemplate db) { this.db = db; }

    @GetMapping("/stock-bajo")
    public List<Map<String,Object>> stockBajo() {
        return db.queryForList("SELECT id,codigo,nombre,stock_actual,stock_minimo,fecha_proximo_pedido " +
            "FROM medicamento WHERE activo=1 AND stock_actual <= stock_minimo ORDER BY stock_actual ASC,id ASC");
    }

    @GetMapping("/vencimientos")
    public List<Map<String,Object>> vencimientos(@RequestParam(defaultValue="30") int dias) {
        if (dias < 0 || dias > 365) throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.BAD_REQUEST,"Dias fuera de rango (0 a 365)");
        return db.queryForList("SELECT l.id,l.medicamento_id,m.nombre,l.numero_lote,l.fecha_vence,l.cantidad_disponible " +
            "FROM lote_medicamento l JOIN medicamento m ON m.id=l.medicamento_id " +
            "WHERE l.cantidad_disponible>0 AND l.fecha_vence <= DATE_ADD(CURRENT_DATE(), INTERVAL ? DAY) " +
            "ORDER BY l.fecha_vence,l.id", dias);
    }
}
