package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Interfaz que define el contrato de la API REST para la entidad Fórmula Médica.
 * Establece las rutas (endpoints), los métodos HTTP permitidos y los formatos de datos
 * esperados para la comunicación externa.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Obtiene una lista con todas las fórmulas médicas registradas en el sistema.
     *
     * @return Respuesta HTTP 200 con la lista de fórmulas médicas.
     * @throws BadRequestException Si ocurre un error durante el procesamiento de la solicitud.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;

}
