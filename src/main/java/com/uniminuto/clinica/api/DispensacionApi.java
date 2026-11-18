package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DispensacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/dispensacion")
public interface DispensacionApi {

    @PostMapping(value = "/procesar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> dispensarMedicamento(@RequestBody DispensacionRq dispensacionRq)
            throws BadRequestException;

    @GetMapping(value = "/movimientos/medicamento/{medicamentoId}",
            produces = {"application/json"})
    ResponseEntity<List<MovimientoInventario>> listarHistorialPorMedicamento(@PathVariable Long medicamentoId)
            throws BadRequestException;

    @GetMapping(value = "/movimientos/formula/{formulaId}",
            produces = {"application/json"})
    ResponseEntity<List<MovimientoInventario>> listarHistorialPorFormula(@PathVariable Long formulaId)
            throws BadRequestException;
}