package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    /**
     * Obtiene todas las citas disponibles en el sistema.
     *
     * @return Respuesta HTTP con la lista completa de citas.
     * @throws BadRequestException Si ocurre una validación o un problema de negocio.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cita>> listarCitas() throws BadRequestException;

    /**
     * Filtra citas por un rango de fechas en formato ISO-8601 o YYYY-MM-DD.
     *
     * @param fechaInicial Fecha inicial del rango de búsqueda.
     * @param fechaFinal Fecha final del rango de búsqueda.
     * @return Lista de citas dentro del rango indicado.
     * @throws BadRequestException Si alguno de los parámetros es nulo, vacío o inválido.
     */
    @GetMapping(value = "/filtrar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cita>> filtrarCitas(
           @RequestParam String fechaInicial,
           @RequestParam String fechaFinal)
           throws BadRequestException;

    /**
     * Crea una nueva cita con la información enviada en el cuerpo de la petición.
     *
     * @param citaRq Datos de la cita que se desea registrar.
     * @return Respuesta con el estado y el mensaje del proceso.
     * @throws BadRequestException Si la información no cumple las validaciones del sistema.
     */
    @PostMapping(value = "/guardar",
           produces = {"application/json"},
           consumes = {"application/json"})
    ResponseEntity<UsuarioRS> guardarCita(
           @RequestBody CitaRq citaRq)
           throws BadRequestException;

    /**
     * Actualiza la información de una cita ya existente.
     *
     * @param citaRq Datos actualizados de la cita.
     * @return Respuesta con el estado y el mensaje del proceso.
     * @throws BadRequestException Si la cita no existe o los datos enviados son inválidos.
     */
    @PostMapping(value = "/actualizar",
           produces = {"application/json"},
           consumes = {"application/json"})
    ResponseEntity<UsuarioRS> actualizarCita(
           @RequestBody CitaRq citaRq)
           throws BadRequestException;
}
