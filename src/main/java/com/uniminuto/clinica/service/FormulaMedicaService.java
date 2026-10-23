package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.FormulaMedicaFiltroRq;
import com.uniminuto.clinica.models.FormulaMedicaResumenRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;

/**
 * Servicio de negocio para la gestión de fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Lista las fórmulas en su vista resumida, paginadas, filtradas y
     * ordenadas. Por defecto de la creada más recientemente a la más antigua.
     *
     * @param filtro filtros, página y ordenamiento.
     * @return página de fórmulas resumidas.
     */
    PaginaRs<FormulaMedicaResumenRs> listarFormulas(FormulaMedicaFiltroRq filtro);

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param formulaId identificador de la fórmula.
     * @return fórmula encontrada.
     */
    FormulaMedica consultarFormula(Long formulaId);

    /**
     * Crea una fórmula médica validando las reglas de negocio.
     *
     * @param rq datos de la fórmula.
     * @return respuesta con el id de la fórmula creada.
     */
    MiRespuestaRS crearFormula(FormulaMedicaRq rq);

    /**
     * Edita una fórmula activa. No se puede cambiar la mascota ni la consulta asociada.
     *
     * @param formulaId identificador de la fórmula.
     * @param rq        nuevos datos.
     * @return respuesta de la operación.
     */
    MiRespuestaRS editarFormula(Long formulaId, FormulaMedicaRq rq);

    /**
     * Inactiva una fórmula. Las fórmulas nunca se eliminan físicamente porque
     * están asociadas a información clínica.
     *
     * @param formulaId identificador de la fórmula.
     * @return respuesta de la operación.
     */
    MiRespuestaRS inactivarFormula(Long formulaId);
}
