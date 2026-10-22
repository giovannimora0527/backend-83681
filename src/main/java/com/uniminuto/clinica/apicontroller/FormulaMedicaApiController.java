package com.uniminuto.clinica.apicontroller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.FormulaVistaRS;
import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del recurso fórmula médica.
 */
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulasMedicas() throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasMedicas());
    }

    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulasPorMascota(Integer mascotaId) throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasPorMascota(mascotaId));
    }

    @Override
    public ResponseEntity<FormulaMedica> consultarFormulaMedica(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.consultarFormulaMedica(id));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarFormulaMedica(FormulaMedicaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.guardarFormulaMedica(formulaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarFormulaMedica(FormulaMedicaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.actualizarFormulaMedica(formulaRq));
    }

    @Override
    public ResponseEntity<PaginaRS<FormulaVistaRS>> listarFormulasPaginadas(
            int pagina, int tamanio, String campo, String direccion,
            Integer mascotaId, Long medicoId, String medicamento,
            LocalDate fechaDesde, LocalDate fechaHasta) throws BadRequestException {
        Filtros.Formula filtros = new Filtros.Formula(mascotaId, medicoId, medicamento, fechaDesde, fechaHasta);
        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasPaginadas(filtros, pagina, tamanio, campo, direccion));
    }
}
