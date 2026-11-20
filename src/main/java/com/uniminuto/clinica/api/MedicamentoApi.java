package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uniminuto.clinica.models.ProductoRq;
import java.time.LocalDate;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medicamento")
public interface MedicamentoApi {


    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos()
            throws BadRequestException;

    @GetMapping("/{id}") ResponseEntity<Medicamento> consultar(@PathVariable Long id);
    @PostMapping(value = "/crear", consumes = "application/json", produces = "application/json") ResponseEntity<Medicamento> crear(@RequestBody ProductoRq producto);
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json") ResponseEntity<Medicamento> actualizar(@PathVariable Long id, @RequestBody ProductoRq producto);
    @GetMapping("/stock-bajo") ResponseEntity<List<Medicamento>> stockBajo();
    @GetMapping("/agotados") ResponseEntity<List<Medicamento>> agotados();
    @GetMapping("/por-vencer") ResponseEntity<List<Medicamento>> porVencer(@RequestParam LocalDate hasta);
    @GetMapping("/vencidos") ResponseEntity<List<Medicamento>> vencidos();

}
