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

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    @Autowired
    private MedicamentoService medicamentoService;

    @Override
    public ResponseEntity<List<Medicamento>> listar() throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.listar());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(MedicamentoRq rq) throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.guardar(rq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(MedicamentoRq rq) throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.actualizar(rq));
    }
}
