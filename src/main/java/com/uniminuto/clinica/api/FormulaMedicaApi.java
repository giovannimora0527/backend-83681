package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulas()
            throws BadRequestException;


    /**
     * Metodo para guardar una formula medica.
     * @return
     * @throws BadRequestException
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormula(
            @RequestBody FormulaRq formulaRq
    )
            throws BadRequestException;


    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormula(
            @RequestBody FormulaRq formulaRq
    )
            throws BadRequestException;

    @PostMapping(value = "/{id}/dispensar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> dispensar(@PathVariable("id") Long id) throws BadRequestException;

    @GetMapping(value = "/{id}.pdf", produces = "application/pdf")
    ResponseEntity<byte[]> descargarPdf(@PathVariable("id") Long id);
}
