package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.CitaVistaRS;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
<<<<<<< HEAD
import com.uniminuto.clinica.models.CerrarConsultaRq;
=======
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

import java.util.List;
import com.uniminuto.clinica.models.DisponibilidadRs;
import java.time.LocalDate;

<<<<<<< HEAD
public interface CitaService {

    List<Cita> listarCitas() throws BadRequestException;

    List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException;

    List<Cita> listarCitasPorMascota(Integer mascotaId) throws BadRequestException;

    MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException;

    MiRespuestaRS cerrarConsulta(Long citaId, CerrarConsultaRq request) throws BadRequestException;

    DisponibilidadRs consultarDisponibilidad(Long medicoId, LocalDate fecha, Integer duracionMinutos);
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
