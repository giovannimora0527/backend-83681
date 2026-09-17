package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Historia_Medica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Historia_MedicaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para operaciones sobre historias médicas.
 * La anotación médica queda en otra API separada.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/historia")
public interface Historia_MedicaApi {

   /**
     * Lista todas las historias en el sistema ordenadas por fecha (más recientes primero).
     * @return ResponseEntity con lista de HistoriaMedica
     * @throws BadRequestException en caso de error de validación
     */
   @GetMapping(value = "/listar",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<List<Historia_Medica>> listarHistorias() throws BadRequestException;

   /**
     * Filtra historias por rango de fechas. Las fechas deben estar en formato ISO o yyyy-MM-dd.
     * @param fechaInicial fecha inicial (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)
     * @param fechaFinal fecha final (yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss)
     * @return ResponseEntity con lista de HistoriaMedica en el rango
     * @throws BadRequestException si parámetros inválidos
     */
   @GetMapping(value = "/filtrar",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<List<Historia_Medica>> filtrarHistorias(
           @RequestParam String fechaInicial,
           @RequestParam String fechaFinal)
           throws BadRequestException;

   /**
     * Guarda una nueva historia médica asociada a un paciente.
     * La fecha de creación se asigna automáticamente si no llega en el payload.
     *
     * @param historiaMediacaRq objeto con el pacienteId y demás datos
     * @return ResponseEntity con el resultado
     * @throws BadRequestException si los datos son inválidos
     */
   @PostMapping(value = "/guardar",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<UsuarioRS> guardarHistoria(
           @RequestBody Historia_MedicaRq historiaMediacaRq)
           throws BadRequestException;

   /**
     * Actualiza una historia médica existente.
     *
     * @param historiaMediacaRq objeto con el id y los cambios
     * @return ResponseEntity con el resultado
     * @throws BadRequestException si la historia no existe o los datos son inválidos
     */
   @PostMapping(value = "/actualizar",
           produces = {"application/json"},
           consumes = {"application/json"})
   ResponseEntity<UsuarioRS> actualizarHistoria(
           @RequestBody Historia_MedicaRq historiaMediacaRq)
           throws BadRequestException;
}
