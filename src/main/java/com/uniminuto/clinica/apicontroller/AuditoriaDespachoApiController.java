package com.uniminuto.clinica.apicontroller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/despacho/auditoria")
public class AuditoriaDespachoApiController {
    private final JdbcTemplate db;
    public AuditoriaDespachoApiController(JdbcTemplate db) { this.db=db; }

    @GetMapping("/{despachoId}")
    public Map<String,Object> consultar(@PathVariable long despachoId) {
        List<Map<String,Object>> cab = db.queryForList(
            "SELECT id,formula_id,usuario_id,fecha,estado FROM despacho WHERE id=?",despachoId);
        if(cab.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Despacho no encontrado");
        List<Map<String,Object>> detalles = db.queryForList(
            "SELECT id,formula_medica_id,lote_id,cantidad,movimiento_id FROM despacho_detalle WHERE despacho_id=? ORDER BY id",despachoId);
        List<Map<String,Object>> movimientos = db.queryForList(
            "SELECT id,fecha,medicamento_id,lote_id,tipo,cantidad,usuario_id,observacion FROM movimiento_inventario WHERE despacho_id=? ORDER BY id",despachoId);
        return Map.of("despacho",cab.get(0),"detalles",detalles,"movimientos",movimientos);
    }
}
