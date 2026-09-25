package com.uniminuto.veterinaria.service;

import com.uniminuto.veterinaria.entity.Cita;
import java.util.List;

/**
 * Interfaz de la capa de servicio para definir las operaciones de negocio del recurso Cita.
 */
public interface CitaService {

    /**
     * Obtiene el listado completo de citas registradas.
     *
     * @return Lista de objetos {@link Cita}.
     */
    List<Cita> listarTodas();

    /**
     * Filtra las citas por un rango de fechas en orden descendente.
     *
     * @param inicio Fecha de inicio en formato YYYY-MM-DD.
     * @param fin    Fecha de fin en formato YYYY-MM-DD.
     * @return Lista de citas dentro del rango especificado.
     */
    List<Cita> filtrarPorFecha(String inicio, String fin);

    /**
     * Registra una nueva cita médica.
     *
     * @param cita Objeto {@link Cita} con los datos a guardar.
     * @return Objeto {@link Cita} guardado en la base de datos.
     */
    Cita guardarCita(Cita cita);

    /**
     * Actualiza la información de una cita existente.
     *
     * @param id   Identificador de la cita a actualizar.
     * @param cita Datos actualizados de la cita.
     * @return Objeto {@link Cita} modificado.
     */
    Cita actualizarCita(Long id, Cita cita);
}