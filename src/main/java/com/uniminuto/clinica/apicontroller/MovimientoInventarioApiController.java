package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MovimientoInventarioApi;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.service.MovimientoInventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** Atiende las consultas HTTP del historial de inventario. */
@RestController
public class MovimientoInventarioApiController implements MovimientoInventarioApi {

    private final MovimientoInventarioService movimientoService;

    /** Construye el controlador con el servicio de movimientos. */
    public MovimientoInventarioApiController(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MovimientoInventario>> listarPorMedicamento(Long medicamentoId) {
        return ResponseEntity.ok(movimientoService.listarPorMedicamento(medicamentoId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MovimientoInventario>> listarTodos() {
        return ResponseEntity.ok(movimientoService.listarTodos());
    }
}
