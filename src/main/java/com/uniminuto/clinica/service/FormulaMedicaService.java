package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/**
 * Contrato de la lógica de negocio de las fórmulas médicas.
 */
public interface FormulaMedicaService {

    /**
     * Lista las fórmulas (información resumida) de la más reciente a la más antigua por fecha de creación.
     *
     * @return fórmulas resumidas.
     */
    List<FormulaResumenRs> listar();

    /**
     * Consulta el detalle completo de una fórmula.
     *
     * @param id identificador de la fórmula.
     * @return la fórmula con todos sus datos.
     */
    FormulaDetalleRs consultar(Long id);

    /**
     * Registra una fórmula validando las reglas de negocio.
     *
     * @param rq datos de la fórmula.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS crear(FormulaMedicaRq rq);

    /**
     * Edita una fórmula activa (no puede cambiar de mascota ni de historia clínica).
     *
     * @param id identificador de la fórmula.
     * @param rq datos nuevos.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS editar(Long id, FormulaMedicaRq rq);

    /**
     * Inactiva una fórmula (reemplaza a la eliminación física).
     *
     * @param id identificador de la fórmula.
     * @return respuesta con el resultado.
     */
    MiRespuestaRS inactivar(Long id);
}
