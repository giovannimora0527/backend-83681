package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<Medicamento>> listarMedicamentos() {
        return ResponseEntity.ok(medicamentoService.listarMedicamentos());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarMedicamento(MedicamentoRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoService.crearMedicamento(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedicamento(MedicamentoRq rq) {
        return ResponseEntity.ok(medicamentoService.actualizarMedicamento(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> eliminarMedicamento(Long id) {
        return ResponseEntity.ok(medicamentoService.eliminarMedicamento(id));
    }
}
