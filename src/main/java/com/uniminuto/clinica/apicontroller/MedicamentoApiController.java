package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del catálogo de medicamentos.
 */
@RestController
public class MedicamentoApiController implements MedicamentoApi {

    @Autowired
    private MedicamentoService medicamentoService;

    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.listarMedicamentos());
    }

    @Override
    public ResponseEntity<Medicamento> buscarMedicamentoPorNombre(String nombre) throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.buscarMedicamentoPorNombre(nombre));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.guardarMedicamento(medicamentoRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicamentoService.actualizarMedicamento(medicamentoRq));
    }
}
