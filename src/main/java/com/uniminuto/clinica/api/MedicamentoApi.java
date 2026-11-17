package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.InventarioLoteRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/** API REST para administrar y consultar el inventario de medicamentos y productos médicos. */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping({"/medicamento", "/inventario/medicamentos"})
public interface MedicamentoApi {

    /** Lista todos los medicamentos y productos registrados. */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos()
            throws BadRequestException;

    /** Busca productos por nombre, código, descripción o presentación. */
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medicamento>> buscarMedicamentos(@RequestParam String termino)
            throws BadRequestException;

    /** Registra un producto y su saldo inicial como entrada trazable. */
    @PostMapping(value = {"/crear", "/guardar"},
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearMedicamento(@RequestBody MedicamentoRq medicamentoRq,
                                                    @AuthenticationPrincipal String usuarioResponsableId)
            throws BadRequestException;

    /** Actualiza datos descriptivos sin permitir cambios directos del saldo. */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedicamento(@RequestBody MedicamentoRq medicamentoRq,
                                                         @AuthenticationPrincipal String usuarioResponsableId)
            throws BadRequestException;

    /** Lista productos con existencias mayores que cero. */
    @GetMapping(value = "/disponibles", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarDisponibles();

    /** Lista productos agotados. */
    @GetMapping(value = "/agotados", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarAgotados();

    /** Lista productos con saldo inferior o igual a su stock mínimo. */
    @GetMapping(value = "/stock-bajo", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarStockBajo();

    /** Lista productos próximos a vencer dentro del rango solicitado. */
    @GetMapping(value = "/proximos-vencer", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarProximosAVencer(@RequestParam(defaultValue = "30") int dias);

    /** Consulta saldo utilizable y si alcanza para la cantidad solicitada. */
    @GetMapping(value = "/{medicamentoId}/disponibilidad", produces = {"application/json"})
    ResponseEntity<java.util.Map<String, Object>> consultarDisponibilidad(
            @PathVariable Long medicamentoId, @RequestParam(defaultValue = "0") Integer cantidadRequerida);

    /** Lista el inventario consultable por el perfil de despacho, con cantidades y precios. */
    @GetMapping(value = "/disponibilidad", produces = "application/json")
    ResponseEntity<List<MedicamentoInventarioRs>> consultarInventarioParaDespacho();

    /** Lista los lotes y vencimientos asociados a un medicamento. */
    @GetMapping(value = "/{medicamentoId}/lotes", produces = "application/json")
    ResponseEntity<List<InventarioLoteRs>> consultarLotesParaDespacho(@PathVariable Long medicamentoId);

    /** Lista los movimientos relacionados con un medicamento. */
    @GetMapping(value = "/{medicamentoId}/movimientos", produces = "application/json")
    ResponseEntity<List<MovimientoInventarioRs>> consultarMovimientosParaDespacho(
            @PathVariable Long medicamentoId);

    /** Descarga un informe parametrizable de existencias, lotes y movimientos en Excel. */
    @GetMapping(value = "/informes/exportar-excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> exportarInformeInventarioExcel(
            @RequestParam(defaultValue = "TODOS") String tipo,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta);

    /** Resumen de inventario sin relaciones JPA, con precios y existencia actual. */
    @Data
    class MedicamentoInventarioRs {
        private Long id;
        private String nombre;
        private String tipoProducto;
        private String presentacion;
        private String registroInvima;
        private BigDecimal precioCompra;
        private BigDecimal precioVenta;
        private Integer cantidadActual;
        private Integer stockMinimo;
        private LocalDate fechaProximoPedido;
    }

    /** Movimiento de inventario plano para su consulta desde el módulo de despacho. */
    @Data
    class MovimientoInventarioRs {
        private Long id;
        private Long medicamentoId;
        private Long loteId;
        private String tipo;
        private Integer cantidad;
        private LocalDateTime fechaMovimiento;
        private Long usuarioId;
        private Long usuarioAutorizacionId;
        private Long formulaId;
        private Long despachoId;
        private Long despachoDetalleId;
        private Long movimientoOriginalId;
        private String observacion;
    }

}
