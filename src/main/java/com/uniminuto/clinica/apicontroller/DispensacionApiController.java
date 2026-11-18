package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.DispensacionApi;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DispensacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.DispensacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DispensacionApiController implements DispensacionApi {

    @Autowired
    private DispensacionService dispensacionService;

    @Override
    public ResponseEntity<MiRespuestaRS> dispensarMedicamento(DispensacionRq dispensacionRq) throws BadRequestException {
        return ResponseEntity.ok(this.dispensacionService.dispensarMedicamento(dispensacionRq));
    }

    @Override
    public ResponseEntity<List<MovimientoInventario>> listarHistorialPorMedicamento(Long medicamentoId) throws BadRequestException {
        return ResponseEntity.ok(this.dispensacionService.listarHistorialPorMedicamento(medicamentoId));
    }

    @Override
    public ResponseEntity<List<MovimientoInventario>> listarHistorialPorFormula(Long formulaId) throws BadRequestException {
        return ResponseEntity.ok(this.dispensacionService.listarHistorialPorFormula(formulaId));
    }
}
