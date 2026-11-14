package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.InventarioLoteActualizacionRq;
import com.uniminuto.clinica.models.InventarioLoteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;

import java.util.List;

/** Define los casos de uso para administrar lotes y registrar sus entradas. */
public interface InventarioLoteService {

    /** Lista todos los lotes ordenados por fecha de vencimiento. */
    List<InventarioLote> listarLotes();

    /** Lista lotes de un medicamento específico. */
    List<InventarioLote> listarLotesPorMedicamento(Long medicamentoId);

    /** Consulta un lote por su identificador. */
    InventarioLote consultarLote(Long id);

    /** Registra un lote de entrada y su movimiento de inventario. */
    MiRespuestaRS guardarLote(InventarioLoteRq inventarioLoteRq, String usuarioResponsableId)
            throws BadRequestException;

    /** Actualiza los datos del lote; los cambios de cantidad deben registrarse como movimientos. */
    MiRespuestaRS actualizarLote(Long id, InventarioLoteActualizacionRq inventarioLoteRq)
            throws BadRequestException;

    /** Desactiva un lote que no conserva existencias. */
    MiRespuestaRS desactivarLote(Long id)
            throws BadRequestException;
}
