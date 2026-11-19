package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/** API de consulta del historial de entradas y salidas de inventario. */
@Tag(name = "Movimientos de inventario", description = "Administración y Despacho Médico. Historial cronológico de movimientos.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/movimiento-inventario")
public interface MovimientoInventarioApi {

    /** Obtiene el historial de movimientos asociados a un medicamento. */
    @Operation(summary = "Consultar movimientos por medicamento")
    @GetMapping(value = "/medicamento/{medicamentoId}",
            produces = "application/json")
    ResponseEntity<List<MovimientoInventario>> listarPorMedicamento(@PathVariable Long medicamentoId)
            throws BadRequestException;

    /** Obtiene el historial completo de movimientos de inventario. */
    @Operation(summary = "Listar movimientos de inventario")
    @GetMapping(value = "/listar",
                produces = "application/json")
    ResponseEntity<List<MovimientoInventario>> listarTodos();
}
