package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Expone la consulta de fórmulas y el registro o edición desde el formulario de un medicamento. */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {


    /** Lista las fórmulas visibles para la cuenta autenticada. Cada veterinario ve las que emitió. */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulas()
            throws BadRequestException;

    /** Busca dentro de las fórmulas visibles para la cuenta autenticada. */
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> buscarFormulas(@RequestParam String termino)
            throws BadRequestException;


    /** Registra una fórmula individual con su vía de administración en una cita abierta. */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormula(
            @RequestBody FormulaRq formulaRq
    )
            throws BadRequestException;


    /** Edita una fórmula individual vigente de la misma cita si todavía no tiene entregas registradas. */
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormula(
            @RequestBody FormulaRq formulaRq
    )
            throws BadRequestException;
}
