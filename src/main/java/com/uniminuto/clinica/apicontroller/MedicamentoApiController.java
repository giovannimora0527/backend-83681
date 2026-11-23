package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    private final MedicamentoService medicamentoService;

    public MedicamentoApiController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos(String busqueda, String tipoProducto)
            throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.listarMedicamentos(busqueda, tipoProducto));
    }

    @Override
    public ResponseEntity<Medicamento> guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        return ResponseEntity.status(201).body(medicamentoService.guardarMedicamento(medicamentoRq));
    }

    @Override
    public ResponseEntity<Medicamento> actualizarMedicamento(MedicamentoRq medicamentoRq)
            throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.actualizarMedicamento(medicamentoRq));
    }

}
