package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Fórmulas médicas", description = "Administración, Veterinaria y Despacho Médico pueden consultar. Solo el veterinario asignado puede registrar o editar.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {


    /** Lista las fórmulas visibles para la cuenta autenticada. Cada veterinario ve las que emitió. */
    @Operation(summary = "Listar fórmulas visibles")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulas()
            throws BadRequestException;

    /** Busca dentro de las fórmulas visibles para la cuenta autenticada. */
    @Operation(summary = "Buscar fórmulas visibles")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> buscarFormulas(@RequestParam String termino)
            throws BadRequestException;


    /** Registra una fórmula individual con su vía de administración en una cita abierta. */
    @Operation(summary = "Registrar fórmula de un medicamento")
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormula(
            @RequestBody FormulaRq formulaRq
    )
            throws BadRequestException;


    /** Edita una fórmula individual vigente de la misma cita si todavía no tiene entregas registradas. */
    @Operation(summary = "Actualizar fórmula individual",
            description = "Requiere una cita abierta. Una fórmula con entregas registradas queda bloqueada para edición.")
    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormula(
            @RequestBody FormulaRq formulaRq
    )
            throws BadRequestException;
}
