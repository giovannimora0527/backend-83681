package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del catálogo de medicamentos.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medicamento")
public interface MedicamentoApi {

    /**
     * Lista los medicamentos ordenados por nombre.
     *
     * @return lista de medicamentos.
     * @throws BadRequestException si ocurre un error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos()
            throws BadRequestException;

    /**
     * Busca un medicamento por nombre.
     *
     * @param nombre nombre del medicamento.
     * @return medicamento encontrado.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/buscar",
            produces = {"application/json"})
    ResponseEntity<Medicamento> buscarMedicamentoPorNombre(
            @RequestParam String nombre)
            throws BadRequestException;

    /**
     * Registra un medicamento.
     *
     * @param medicamentoRq datos del medicamento.
     * @return resultado de la operación.
     * @throws BadRequestException si los datos no son válidos.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMedicamento(
            @RequestBody MedicamentoRq medicamentoRq)
            throws BadRequestException;

    /**
     * Actualiza un medicamento existente.
     *
     * @param medicamentoRq datos actualizados.
     * @return resultado de la operación.
     * @throws BadRequestException si no existe o los datos no son válidos.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedicamento(
            @RequestBody MedicamentoRq medicamentoRq)
            throws BadRequestException;
}
