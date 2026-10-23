package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de formulas medicas. Recibe la peticion HTTP y delega al servicio.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    /**
     * Servicio con la logica de negocio de las formulas.
     */
    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulas() throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.listarFormulas());
    }

    @Override
    public ResponseEntity<List<FormulaMedica>> getFormulasByOrden(boolean asc) throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.listarFormulasOrdenadas(asc));
    }

    @Override
    public ResponseEntity<FormulaMedica> consultarFormula(Long formulaId) throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.consultarFormula(formulaId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED).body(formulaMedicaService.crearFormula(formulaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.actualizarFormula(formulaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> inactivarFormula(Long formulaId) throws BadRequestException {
        return ResponseEntity.ok(formulaMedicaService.inactivarFormula(formulaId));
    }
}
