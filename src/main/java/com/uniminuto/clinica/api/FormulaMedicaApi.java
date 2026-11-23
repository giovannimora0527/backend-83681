package com.uniminuto.clinica.api;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.FormulaVistaRS;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
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
=======
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrato REST del recurso fórmula médica.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/formula-medica")
public interface FormulaMedicaApi {

    /**
     * Lista todas las fórmulas, de la más reciente a la más antigua.
     *
     * @return lista de fórmulas.
     * @throws BadRequestException si ocurre un error de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            throws BadRequestException;


    /**
<<<<<<< HEAD
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
=======
     * Lista las fórmulas de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return fórmulas de la mascota.
     * @throws BadRequestException si la mascota no existe.
     */
    @GetMapping(value = "/listar-por-mascota",
            produces = {"application/json"})
    ResponseEntity<List<FormulaMedica>> listarFormulasPorMascota(
            @RequestParam Integer mascotaId)
            throws BadRequestException;

    /**
     * Consulta una fórmula por su identificador.
     *
     * @param id identificador de la fórmula.
     * @return fórmula encontrada.
     * @throws BadRequestException si no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<FormulaMedica> consultarFormulaMedica(
            @RequestParam Long id)
            throws BadRequestException;

    /**
     * Registra una fórmula médica asociada a mascota, historia o cita, veterinario y medicamento.
     *
     * @param formulaRq datos de la fórmula.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarFormulaMedica(
            @RequestBody FormulaMedicaRq formulaRq)
            throws BadRequestException;

    /**
     * Actualiza los datos clínicos de una fórmula (medicamento, dosis, frecuencia, duración e indicaciones).
     *
     * @param formulaRq datos actualizados.
     * @return resultado de la operación.
     * @throws BadRequestException si no cumple las reglas de negocio.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarFormulaMedica(
            @RequestBody FormulaMedicaRq formulaRq)
            throws BadRequestException;

    /**
     * Listado paginado y filtrable de fórmulas médicas (DataTable).
     *
     * @return página de fórmulas en vista resumida.
     * @throws BadRequestException si la paginación o los filtros no son válidos.
     */
    @GetMapping(value = "/paginado", produces = {"application/json"})
    ResponseEntity<PaginaRS<FormulaVistaRS>> listarFormulasPaginadas(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio,
            @RequestParam(required = false) String campo,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) Integer mascotaId,
            @RequestParam(required = false) Long medicoId,
            @RequestParam(required = false) String medicamento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta)
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
            throws BadRequestException;

    @PostMapping(value = "/{id}/dispensar", produces = {"application/json"})
    ResponseEntity<MiRespuestaRS> dispensar(@PathVariable("id") Long id) throws BadRequestException;

    @GetMapping(value = "/{id}.pdf", produces = "application/pdf")
    ResponseEntity<byte[]> descargarPdf(@PathVariable("id") Long id);
}
