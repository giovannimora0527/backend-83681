package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDate;
import com.uniminuto.clinica.models.ProductoRq;

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    @Autowired
    private MedicamentoService medicamentoService;

    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.listarMedicamentos());
    }

    @Override public ResponseEntity<Medicamento> consultar(Long id) { return ResponseEntity.ok(medicamentoService.consultar(id)); }
    @Override public ResponseEntity<Medicamento> crear(ProductoRq producto) { return ResponseEntity.status(201).body(medicamentoService.crear(producto)); }
    @Override public ResponseEntity<Medicamento> actualizar(Long id, ProductoRq producto) { return ResponseEntity.ok(medicamentoService.actualizar(id, producto)); }
    @Override public ResponseEntity<List<Medicamento>> stockBajo() { return ResponseEntity.ok(medicamentoService.listarStockBajo()); }
    @Override public ResponseEntity<List<Medicamento>> agotados() { return ResponseEntity.ok(medicamentoService.listarAgotados()); }
    @Override public ResponseEntity<List<Medicamento>> porVencer(LocalDate hasta) { return ResponseEntity.ok(medicamentoService.listarPorVencer(hasta)); }
    @Override public ResponseEntity<List<Medicamento>> vencidos() { return ResponseEntity.ok(medicamentoService.listarVencidos()); }

}
