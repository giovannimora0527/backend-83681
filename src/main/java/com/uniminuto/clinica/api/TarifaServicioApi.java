package com.uniminuto.clinica.api;
import com.uniminuto.clinica.entity.TarifaServicio;
import com.uniminuto.clinica.models.TarifaServicioRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RequestMapping("/tarifas") @CrossOrigin(origins="*", allowedHeaders="*")
public interface TarifaServicioApi {
    @GetMapping("/listar") ResponseEntity<List<TarifaServicio>> listar();
    @GetMapping("/historial") ResponseEntity<List<TarifaServicio>> historial(@RequestParam String nombre);
    @PostMapping(value="/crear", consumes="application/json", produces="application/json") ResponseEntity<TarifaServicio> crear(@RequestBody TarifaServicioRq request);
    @PutMapping(value="/{id}", consumes="application/json", produces="application/json") ResponseEntity<TarifaServicio> actualizar(@PathVariable Long id, @RequestBody TarifaServicioRq request);
}
