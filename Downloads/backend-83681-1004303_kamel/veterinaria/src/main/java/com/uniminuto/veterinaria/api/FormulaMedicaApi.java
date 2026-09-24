package com.uniminuto.veterinaria.api;

import com.uniminuto.veterinaria.entity.FormulaMedica;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Interfaz API que define el contrato HTTP y los endpoints REST
 * para la gestión del recurso de Fórmulas Médicas.
 */
@RequestMapping("/api/formulas")
public interface FormulaMedicaApi {

    /**
     * Endpoint para consultar el listado de fórmulas médicas ordenadas por fecha descendente.
     *
     * @return ResponseEntity con la lista de objetos {@link FormulaMedica} y estado HTTP 200 (OK).
     */
    @GetMapping
    ResponseEntity<List<FormulaMedica>> listarTodas();

    /**
     * Endpoint para registrar una nueva fórmula médica en el sistema.
     *
     * @param formulaMedica Objeto {@link FormulaMedica} enviado en el cuerpo de la petición.
     * @return ResponseEntity con la fórmula médica creada y estado HTTP 200 (OK).
     */
    @PostMapping
    ResponseEntity<FormulaMedica> crearFormula(@RequestBody FormulaMedica formulaMedica);

    /**
     * Endpoint para actualizar los datos de una fórmula médica existente por su identificador único.
     *
     * @param id            Identificador primario de la fórmula médica a modificar.
     * @param formulaMedica Objeto {@link FormulaMedica} con la información actualizada.
     * @return ResponseEntity con la fórmula médica modificada y estado HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    ResponseEntity<FormulaMedica> actualizarFormula(@PathVariable("id") Long id, @RequestBody FormulaMedica formulaMedica);
}