package com.uniminuto.veterinaria.api;

import com.uniminuto.veterinaria.entity.HistoriaMedica;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Interfaz API que define el contrato HTTP y los endpoints REST
 * para la gestión del recurso de Historias Médicas.
 */
@RequestMapping("/api/historias-medicas")
public interface HistoriaMedicaApi {

    /**
     * Endpoint para consultar el listado completo de historias médicas registradas.
     *
     * @return ResponseEntity con la lista de objetos {@link HistoriaMedica} y estado HTTP 200 (OK).
     */
    @GetMapping
    ResponseEntity<List<HistoriaMedica>> listarTodas();

    /**
     * Endpoint para filtrar historias médicas por rango de fechas en orden descendente.
     *
     * @param inicio Fecha inicial del rango de búsqueda en formato YYYY-MM-DD.
     * @param fin    Fecha final del rango de búsqueda en formato YYYY-MM-DD.
     * @return ResponseEntity con la lista de historias médicas filtradas y estado HTTP 200 (OK).
     */
    @GetMapping("/filtrar")
    ResponseEntity<List<HistoriaMedica>> filtrarPorFecha(
            @RequestParam("inicio") String inicio,
            @RequestParam("fin") String fin);

    /**
     * Endpoint para registrar una nueva historia médica en el sistema.
     *
     * @param historiaMedica Objeto {@link HistoriaMedica} enviado en el cuerpo de la petición.
     * @return ResponseEntity con la historia médica creada y estado HTTP 200 (OK).
     */
    @PostMapping
    ResponseEntity<HistoriaMedica> crearHistoriaMedica(@RequestBody HistoriaMedica historiaMedica);

    /**
     * Endpoint para actualizar los datos de una historia médica existente por su identificador único.
     *
     * @param id             Identificador primario de la historia médica a modificar.
     * @param historiaMedica Objeto {@link HistoriaMedica} con la información actualizada.
     * @return ResponseEntity con la historia médica modificada y estado HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    ResponseEntity<HistoriaMedica> actualizarHistoriaMedica(@PathVariable("id") Long id, @RequestBody HistoriaMedica historiaMedica);
}