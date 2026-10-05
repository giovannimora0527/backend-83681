package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    private final MedicamentoService service;

    public MedicamentoApiController(MedicamentoService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(service.listarMedicamentos());
    }

    @Override
    public ResponseEntity<Medicamento> guardarMedicamento(Medicamento medicamento) throws BadRequestException {
        return ResponseEntity.ok(service.guardarMedicamento(medicamento));
    }

    @Override
    public ResponseEntity<Medicamento> actualizarMedicamento(Medicamento medicamento) throws BadRequestException {
        return ResponseEntity.ok(service.actualizarMedicamento(medicamento));
    }
}
