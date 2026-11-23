package com.uniminuto.clinica.apicontroller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.FormulaVistaRS;
import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaService;
=======
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.FormulaMedicaService;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

<<<<<<< HEAD
=======
/**
 * Controlador REST del recurso fórmula médica.
 */
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaService formulaService;

    @Override
<<<<<<< HEAD
    public ResponseEntity<List<FormulaMedica>> listarFormulas() {
        return ResponseEntity.ok(this.formulaService.findAllFormulas());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarFormula(FormulaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(this.formulaService.guardarFormula(formulaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarFormula(FormulaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(this.formulaService.actualizarFormula(formulaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> dispensar(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.formulaService.dispensar(id));
    }

    @Override
    public ResponseEntity<byte[]> descargarPdf(Long id) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"formula-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF).body(formulaService.generarPdf(id));
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    }
}
