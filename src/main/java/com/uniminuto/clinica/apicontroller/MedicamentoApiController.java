package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
<<<<<<< HEAD
=======
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
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

<<<<<<< HEAD
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
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    }

}
