package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.HistoriaVistaRS;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre historias clínicas.
 */
public interface HistoriaMedicaService {

    List<HistoriaMedica> listarHistorias();

    List<HistoriaMedica> filtrarHistorias(String fechaInicial, String fechaFinal) throws BadRequestException;

    List<HistoriaMedica> listarHistoriasPorMascota(Integer mascotaId) throws BadRequestException;

    HistoriaMedica consultarHistoria(Long id) throws BadRequestException;

    MiRespuestaRS guardarHistoria(HistoriaMedicaRq historiaRq) throws BadRequestException;

    MiRespuestaRS actualizarHistoria(HistoriaMedicaRq historiaRq) throws BadRequestException;

    /**
     * Lista paginada y filtrable de historias clínicas, para el DataTable.
     *
     * @param filtros   filtros opcionales (cliente, mascota, veterinario, fechas, texto).
     * @param pagina    número de página, desde 0.
     * @param tamanio   registros por página (1 a 100).
     * @param campo     campo de orden (fechaHora, motivo, diagnostico); por defecto fechaHora.
     * @param direccion "asc" o "desc"; por defecto descendente.
     * @return página de historias en vista resumida.
     * @throws BadRequestException si la paginación o los filtros no son válidos.
     */
    PaginaRS<HistoriaVistaRS> listarHistoriasPaginadas(Filtros.Historia filtros, int pagina, int tamanio,
                                                       String campo, String direccion) throws BadRequestException;
}
