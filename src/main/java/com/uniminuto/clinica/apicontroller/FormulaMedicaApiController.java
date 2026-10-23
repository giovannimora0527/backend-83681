package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador de las fórmulas médicas: delega toda la lógica en {@link FormulaMedicaService}.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    /** Servicio con la lógica de negocio de fórmulas. */
    @Autowired
    private FormulaMedicaService formulaService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<FormulaResumenRs>> listar() {
        return ResponseEntity.ok(formulaService.listar());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<FormulaDetalleRs> consultar(Long id) {
        return ResponseEntity.ok(formulaService.consultar(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> crear(FormulaMedicaRq rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(formulaService.crear(rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> editar(Long id, FormulaMedicaRq rq) {
        return ResponseEntity.ok(formulaService.editar(id, rq));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> inactivar(Long id) {
        return ResponseEntity.ok(formulaService.inactivar(id));
    }
}
