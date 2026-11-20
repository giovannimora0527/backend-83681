package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.InventarioMovimiento;
import com.uniminuto.clinica.models.EntradaLoteRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/inventario")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface InventarioApi {
    @PostMapping("/lotes/entrada") ResponseEntity<InventarioLote> registrarEntrada(@RequestBody EntradaLoteRq request);
    @GetMapping("/productos/{id}/lotes") ResponseEntity<List<InventarioLote>> lotes(@PathVariable Long id);
    @GetMapping("/productos/{id}/movimientos") ResponseEntity<List<InventarioMovimiento>> movimientos(@PathVariable Long id);
    @GetMapping("/lotes/por-vencer") ResponseEntity<List<InventarioLote>> porVencer(@RequestParam LocalDate hasta);
}
