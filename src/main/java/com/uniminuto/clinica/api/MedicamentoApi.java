package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/medicamento")
public interface MedicamentoApi {


    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos(
            @RequestParam(name = "busqueda", required = false) String busqueda,
            @RequestParam(name = "tipoProducto", required = false) String tipoProducto)
            throws BadRequestException;

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Medicamento> guardarMedicamento(@RequestBody MedicamentoRq medicamentoRq)
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Medicamento> actualizarMedicamento(@RequestBody MedicamentoRq medicamentoRq)
            throws BadRequestException;

}
