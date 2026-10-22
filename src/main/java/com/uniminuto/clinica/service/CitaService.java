package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.CitaVistaRS;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre citas.
 */
public interface CitaService {

    List<Cita> listarCitas();

    List<Cita> filtrarCitas(String fechaInicial, String fechaFinal) throws BadRequestException;

    Cita consultarCita(Long id) throws BadRequestException;

    MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS reactivarCita(Long id) throws BadRequestException;

    /**
     * Lista paginada y filtrable de citas, para el DataTable.
     *
     * @param filtros   filtros opcionales (cliente, mascota, veterinario, estado, fechas, texto).
     * @param pagina    número de página, desde 0.
     * @param tamanio   registros por página (1 a 100).
     * @param campo     campo de orden (fechaHora, estado, motivo); por defecto fechaHora.
     * @param direccion "asc" o "desc"; por defecto descendente.
     * @return página de citas en vista resumida.
     * @throws BadRequestException si la paginación o los filtros no son válidos.
     */
    PaginaRS<CitaVistaRS> listarCitasPaginadas(Filtros.Cita filtros, int pagina, int tamanio,
                                               String campo, String direccion) throws BadRequestException;
}
