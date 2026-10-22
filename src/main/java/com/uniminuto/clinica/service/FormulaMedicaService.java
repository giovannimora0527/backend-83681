package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.FormulaVistaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRS;

import java.util.List;

/**
 * Operaciones de negocio sobre fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Lista todas las fórmulas, de la más reciente a la más antigua.
     *
     * @return fórmulas ordenadas por fecha de creación descendente.
     */
    List<FormulaMedica> listarFormulasMedicas();

    /**
     * Lista paginada y filtrable de fórmulas, para el DataTable.
     *
     * @param filtros  filtros opcionales.
     * @param pagina   número de página, desde 0.
     * @param tamanio  registros por página.
     * @param campo    campo de orden (opcional).
     * @param direccion "asc" o "desc" (opcional).
     * @return página de vistas resumidas.
     * @throws BadRequestException si la paginación no es válida.
     */
    PaginaRS<FormulaVistaRS> listarFormulasPaginadas(Filtros.Formula filtros, int pagina, int tamanio,
                                                     String campo, String direccion) throws BadRequestException;

    /**
     * Lista las fórmulas de una mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @return fórmulas de la mascota.
     * @throws BadRequestException si la mascota no existe.
     */
    List<FormulaMedica> listarFormulasPorMascota(Integer mascotaId) throws BadRequestException;

    /**
     * Consulta el detalle de una fórmula.
     *
     * @param id identificador de la fórmula.
     * @return fórmula encontrada.
     * @throws BadRequestException si no existe.
     */
    FormulaMedica consultarFormulaMedica(Long id) throws BadRequestException;

    /**
     * Registra una nueva fórmula.
     *
     * @param formulaRq datos de la fórmula.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos o las reglas de negocio no se cumplen.
     */
    MiRespuestaRS guardarFormulaMedica(FormulaMedicaRq formulaRq) throws BadRequestException;

    /**
     * Actualiza los datos clínicos permitidos de una fórmula.
     *
     * @param formulaRq datos de la fórmula, con id.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos o las reglas de negocio no se cumplen.
     */
    MiRespuestaRS actualizarFormulaMedica(FormulaMedicaRq formulaRq) throws BadRequestException;
}
