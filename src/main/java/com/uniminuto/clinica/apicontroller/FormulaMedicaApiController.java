package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaMedicaFiltroRq;
import com.uniminuto.clinica.models.FormulaMedicaResumenRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST que implementa {@link FormulaMedicaApi}.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    /**
     * Servicio de fórmulas médicas.
     */
    private final FormulaMedicaService formulaService;

    public FormulaMedicaApiController(FormulaMedicaService formulaService) {
        this.formulaService = formulaService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<PaginaRs<FormulaMedicaResumenRs>> listarFormulas(FormulaMedicaFiltroRq filtro) {
        return ResponseEntity.ok(formulaService.listarFormulas(filtro));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<FormulaMedica> consultarFormula(Long formulaId) {
        return ResponseEntity.ok(formulaService.consultarFormula(formulaId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> crearFormula(FormulaMedicaRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formulaService.crearFormula(rq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> editarFormula(Long formulaId, FormulaMedicaRq rq) {
        return ResponseEntity.ok(formulaService.editarFormula(formulaId, rq));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<MiRespuestaRS> inactivarFormula(Long formulaId) {
        return ResponseEntity.ok(formulaService.inactivarFormula(formulaId));
    }
}
