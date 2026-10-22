package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.AnotacionVistaRS;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre anotaciones de historia clínica.
 */
public interface AnotacionHistoriaService {

    List<AnotacionHistoria> listarAnotaciones(Long historiaId) throws BadRequestException;

    List<AnotacionHistoria> filtrarAnotaciones(Long historiaId, String fechaInicial, String fechaFinal) throws BadRequestException;

    AnotacionHistoria consultarAnotacion(Long id) throws BadRequestException;

    MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;

    MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException;

    /**
     * Histórico paginado de anotaciones de una historia clínica, para el DataTable del detalle.
     *
     * @param filtros   filtros (historia obligatoria, fechas y texto en la descripción).
     * @param pagina    número de página, desde 0.
     * @param tamanio   registros por página (1 a 100).
     * @param campo     campo de orden (fechaHora, descripcion); por defecto fechaHora.
     * @param direccion "asc" o "desc"; por defecto descendente (más recientes primero).
     * @return página de anotaciones en vista.
     * @throws BadRequestException si la historia no existe o la paginación no es válida.
     */
    PaginaRS<AnotacionVistaRS> listarAnotacionesPaginadas(Filtros.Anotacion filtros, int pagina, int tamanio,
                                                          String campo, String direccion) throws BadRequestException;
}
