package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CambioEstadoCitaRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.models.ReactivarCitaRq;

import java.util.List;

/**
 * Servicio de negocio para la gestión de citas médicas.
 * Todas las reglas críticas (pertenencia mascota-cliente, disponibilidad del
 * veterinario y de la mascota, fechas y estados) se validan aquí.
 */
public interface CitaService {

    /**
     * Lista las citas de forma paginada, filtrada y ordenada. Por defecto se
     * ordena por fecha y hora de la cita, de la más reciente a la más antigua.
     *
     * @param filtro filtros, página y ordenamiento.
     * @return página de citas.
     */
    PaginaRs<Cita> listarCitas(CitaFiltroRq filtro);

    /**
     * Consulta una cita por su identificador.
     *
     * @param citaId identificador de la cita.
     * @return cita encontrada.
     */
    Cita consultarCita(Long citaId);

    /**
     * Crea una cita validando todas las reglas de negocio.
     *
     * @param citaRq datos de la cita.
     * @return respuesta con el id de la cita creada.
     */
    MiRespuestaRS crearCita(CitaRq citaRq);

    /**
     * Edita la fecha, hora, veterinario o motivo de una cita activa.
     * Las citas canceladas o atendidas no se pueden editar.
     *
     * @param citaId identificador de la cita.
     * @param citaRq nuevos datos.
     * @return respuesta de la operación.
     */
    MiRespuestaRS editarCita(Long citaId, CitaRq citaRq);

    /**
     * Cambia el estado de una cita respetando las transiciones permitidas.
     *
     * @param citaId identificador de la cita.
     * @param rq     nuevo estado.
     * @return respuesta de la operación.
     */
    MiRespuestaRS cambiarEstado(Long citaId, CambioEstadoCitaRq rq);

    /**
     * Reactiva una cita cancelada (operación explícita). La cita vuelve a
     * estado PENDIENTE en una fecha futura y con disponibilidad.
     *
     * @param citaId identificador de la cita.
     * @param rq     nueva fecha opcional.
     * @return respuesta de la operación.
     */
    MiRespuestaRS reactivarCita(Long citaId, ReactivarCitaRq rq);

    /**
     * Lista las citas atendidas de una mascota que todavía no tienen historia
     * clínica (combo dependiente del formulario de historias).
     *
     * @param mascotaId identificador de la mascota.
     * @return citas disponibles.
     */
    List<Cita> listarAtendidasSinHistoria(Long mascotaId);
}
