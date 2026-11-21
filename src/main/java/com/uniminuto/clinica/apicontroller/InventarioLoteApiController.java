package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.service.InventarioLoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventario/lotes")
public class InventarioLoteApiController {
    private final InventarioLoteService servicio;

    public InventarioLoteApiController(InventarioLoteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/medicamento/{medicamentoId}")
    public ResponseEntity<List<InventarioLoteService.LoteRespuesta>> listar(
            @PathVariable Long medicamentoId) {
        return ResponseEntity.ok(servicio.listar(medicamentoId));
    }

    @PostMapping
    public ResponseEntity<InventarioLoteService.LoteRespuesta> registrar(
            @RequestBody InventarioLoteService.NuevoLote datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(datos));
    }
}
