package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/** API de consulta del historial de entradas y salidas de inventario. */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/movimiento-inventario")
public interface MovimientoInventarioApi {

    /** Obtiene el historial de movimientos asociados a un medicamento. */
    @GetMapping(value = "/medicamento/{medicamentoId}",
            produces = "application/json")
    ResponseEntity<List<MovimientoInventario>> listarPorMedicamento(@PathVariable Long medicamentoId)
            throws BadRequestException;

    /** Obtiene el historial completo de movimientos de inventario. */
    @GetMapping(value = "/listar",
                produces = "application/json")
    ResponseEntity<List<MovimientoInventario>> listarTodos();
}
