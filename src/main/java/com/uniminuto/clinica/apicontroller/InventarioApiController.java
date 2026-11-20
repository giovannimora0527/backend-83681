package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.InventarioApi;
import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.InventarioMovimiento;
import com.uniminuto.clinica.models.EntradaLoteRq;
import com.uniminuto.clinica.service.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
public class InventarioApiController implements InventarioApi {
    private final InventarioService inventario;
    public InventarioApiController(InventarioService inventario) { this.inventario = inventario; }
    @Override public ResponseEntity<InventarioLote> registrarEntrada(EntradaLoteRq request) { return ResponseEntity.status(201).body(inventario.registrarEntrada(request)); }
    @Override public ResponseEntity<List<InventarioLote>> lotes(Long id) { return ResponseEntity.ok(inventario.consultarLotes(id)); }
    @Override public ResponseEntity<List<InventarioMovimiento>> movimientos(Long id) { return ResponseEntity.ok(inventario.movimientos(id)); }
    @Override public ResponseEntity<List<InventarioLote>> porVencer(LocalDate hasta) { return ResponseEntity.ok(inventario.consultarPorVencimiento(hasta)); }
}
