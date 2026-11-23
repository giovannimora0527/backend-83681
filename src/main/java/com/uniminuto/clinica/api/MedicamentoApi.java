package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
<<<<<<< HEAD
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/medicamento")
public interface MedicamentoApi {


    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos(
            @RequestParam(name = "busqueda", required = false) String busqueda,
            @RequestParam(name = "tipoProducto", required = false) String tipoProducto)
            throws BadRequestException;

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Medicamento> guardarMedicamento(@RequestBody MedicamentoRq medicamentoRq)
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Medicamento> actualizarMedicamento(@RequestBody MedicamentoRq medicamentoRq)
            throws BadRequestException;

}
