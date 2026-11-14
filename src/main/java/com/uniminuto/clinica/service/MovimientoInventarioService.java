package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.MovimientoInventario;
import java.util.List;

/** Define las consultas del historial de movimientos de inventario. */
public interface MovimientoInventarioService {

    /** Lista los movimientos registrados para un medicamento. */
    List<MovimientoInventario> listarPorMedicamento(Long medicamentoId);

    /** Lista todos los movimientos registrados. */
    List<MovimientoInventario> listarTodos();
}
