package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping; // <-- No olvides importar esto
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador que implementa los endpoints definidos en la interfaz FormulaMedicaApi.
 * Proporciona funcionalidades para listar y consultar fórmulas médicas.
 */
@RestController
@RequestMapping("/formula_medica") // <-- ¡Agrega esto aquí!
public class FormulaMedicaApiController implements FormulaMedicaApi {

    /**
     * Servicio que maneja la lógica de negocio relacionada con las fórmulas médicas.
     */
    @Autowired
    private FormulaMedicaService formulaMedicaService;

    /**
     * Endpoint para listar todas las fórmulas médicas registradas en el sistema.
     *
     * @return Respuesta HTTP con la lista completa de fórmulas médicas.
     * @throws BadRequestException Si ocurre una validación o un problema de negocio.
     */
    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulaMedica() throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasMedicas());
    }

    /**
     * Endpoint dedicado solo a consultar fórmulas médicas por el alias /formulas.
     *
     * @return Respuesta HTTP con la lista de fórmulas médicas.
     * @throws BadRequestException Si ocurre una validación o un problema de negocio.
     */
    @Override
    public ResponseEntity<List<FormulaMedica>> consultarFormulasMedicas() throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasMedicas());
    }
}