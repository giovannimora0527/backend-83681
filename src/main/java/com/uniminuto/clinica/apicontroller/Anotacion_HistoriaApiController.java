package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.Anotacion_HistoriaApi;
import com.uniminuto.clinica.entity.Anotacion_Historia;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Anotacion_HistoriaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.service.Anotacion_HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementación del controlador REST para la gestión de anotaciones de historia clínica.
 * Centraliza la validación de parámetros y delega la lógica de negocio al servicio.
 */
@RestController
public class Anotacion_HistoriaApiController implements Anotacion_HistoriaApi {

    /** Servicio para gestionar anotaciones de historias médicas. */
    @Autowired
    private Anotacion_HistoriaService anotacionHistoriaService;

    /**
     * Lista todas las anotaciones de una historia médica.
     *
     * @param historiaId identificador de la historia
     * @return respuesta HTTP con la lista de anotaciones
     * @throws BadRequestException si la historia no existe o no se envía el id
     */
    @Override
    public ResponseEntity<List<Anotacion_Historia>> listarAnotaciones(Long historiaId) throws BadRequestException {
       return ResponseEntity.ok(this.anotacionHistoriaService.listarAnotacionesByHistoria(historiaId));
    }

    /**
     * Lista las anotaciones de una historia dentro de un rango de fechas.
     *
     * @param historiaId identificador de la historia
     * @param fechaInicial fecha inicial del filtro
     * @param fechaFinal fecha final del filtro
     * @return respuesta HTTP con la lista filtrada
     * @throws BadRequestException si algún parámetro es inválido
     */
    @Override
    public ResponseEntity<List<Anotacion_Historia>> filtrarAnotaciones(Long historiaId, String fechaInicial, String fechaFinal) throws BadRequestException {
       return ResponseEntity.ok(this.anotacionHistoriaService.filtrarAnotaciones(historiaId, fechaInicial, fechaFinal));
    }

    /**
     * Crea una nueva anotación médica.
     *
     * @param rq payload con los datos de la anotación
     * @return respuesta HTTP con el resultado del proceso
     * @throws BadRequestException si la validación falla
     */
    @Override
    public ResponseEntity<UsuarioRS> guardarAnotacion(Anotacion_HistoriaRq rq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.guardarAnotacion(rq));
    }

    /**
     * Actualiza una anotación médica existente.
     *
     * @param rq payload con los campos a actualizar
     * @return respuesta HTTP con el resultado del proceso
     * @throws BadRequestException si los datos no son válidos
     */
    @Override
    public ResponseEntity<UsuarioRS> actualizarAnotacion(Anotacion_HistoriaRq rq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.actualizarAnotacion(rq));
    }

}
