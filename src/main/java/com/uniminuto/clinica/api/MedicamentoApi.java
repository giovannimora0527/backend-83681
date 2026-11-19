package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.models.InformeInventarioRs;
import java.io.IOException;

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
@Tag(name = "Medicamentos e informes de inventario", description = "Administración gestiona medicamentos. Veterinaria y Despacho Médico pueden consultar. Los informes se reservan a Administración y Despacho Médico.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping({"/medicamento", "/inventario/medicamentos"})
public interface MedicamentoApi {

    /** Lista todos los medicamentos y productos registrados. */
    @Operation(summary = "Listar medicamentos")
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medicamento>> listarMedicamentos()
            throws BadRequestException;

    /** Busca productos por nombre, código, descripción o presentación. */
    @Operation(summary = "Buscar medicamentos")
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medicamento>> buscarMedicamentos(@RequestParam String termino)
            throws BadRequestException;

    /** Registra un producto y su saldo inicial como entrada trazable. */
    @Operation(summary = "Registrar medicamento")
    @PostMapping(value = {"/crear", "/guardar"},
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> crearMedicamento(@RequestBody MedicamentoRq medicamentoRq,
                                                    @AuthenticationPrincipal String usuarioResponsableId)
            throws BadRequestException;

    /** Actualiza datos descriptivos sin permitir cambios directos del saldo. */
    @Operation(summary = "Actualizar medicamento")
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMedicamento(@RequestBody MedicamentoRq medicamentoRq,
                                                         @AuthenticationPrincipal String usuarioResponsableId)
            throws BadRequestException;

    /** Lista productos con existencias mayores que cero. */
    @Operation(summary = "Listar medicamentos disponibles")
    @GetMapping(value = "/disponibles", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarDisponibles();

    /** Lista productos agotados. */
    @Operation(summary = "Listar medicamentos agotados")
    @GetMapping(value = "/agotados", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarAgotados();

    /** Lista productos con saldo inferior o igual a su stock mínimo. */
    @Operation(summary = "Listar medicamentos con stock bajo")
    @GetMapping(value = "/stock-bajo", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarStockBajo();

    /** Lista productos próximos a vencer dentro del rango solicitado. */
    @Operation(summary = "Listar medicamentos próximos a vencer")
    @GetMapping(value = "/proximos-vencer", produces = {"application/json"})
    ResponseEntity<List<Medicamento>> listarProximosAVencer(@RequestParam(defaultValue = "30") int dias);

    /** Consulta saldo utilizable y si alcanza para la cantidad solicitada. */
    @Operation(summary = "Consultar disponibilidad de un medicamento")
    @GetMapping(value = "/{medicamentoId}/disponibilidad", produces = {"application/json"})
    ResponseEntity<java.util.Map<String, Object>> consultarDisponibilidad(
            @PathVariable Long medicamentoId, @RequestParam(defaultValue = "0") Integer cantidadRequerida);

    /** Lista el inventario consultable por el perfil de despacho, con cantidades y precios. */
    @Operation(summary = "Consultar disponibilidad general para despacho")
    @GetMapping(value = "/disponibilidad", produces = "application/json")
    ResponseEntity<List<MedicamentoInventarioRs>> consultarInventarioParaDespacho();

    /** Lista los lotes y vencimientos asociados a un medicamento. */
    @Operation(summary = "Consultar lotes para despacho")
    @GetMapping(value = "/{medicamentoId}/lotes", produces = "application/json")
    ResponseEntity<List<InventarioLoteRs>> consultarLotesParaDespacho(@PathVariable Long medicamentoId);

    /** Lista los movimientos relacionados con un medicamento. */
    @Operation(summary = "Consultar movimientos de un medicamento")
    @GetMapping(value = "/{medicamentoId}/movimientos", produces = "application/json")
    ResponseEntity<List<MovimientoInventarioRs>> consultarMovimientosParaDespacho(
            @PathVariable Long medicamentoId);

    /** Descarga un informe parametrizable de existencias, lotes y movimientos en Excel. */
    @Operation(summary = "Descargar informe de inventario en Excel",
            description = "El rango de fechas se aplica a los movimientos.")
    @GetMapping(value = "/informes/exportar-excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ResponseEntity<byte[]> exportarInformeInventarioExcel(
            @RequestParam(defaultValue = "TODOS") String tipo,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta);

    /** Genera la información del inventario con el tipo de informe y las fechas recibidas. */
    @Operation(summary = "Consultar vista previa de inventario",
            description = "Usa los mismos filtros y totales de las descargas.")
    @GetMapping(value = "/informes", produces = "application/json")
    InformeInventarioRs consultarInformeInventario(
            @RequestParam(defaultValue = "TODOS") String tipo,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta);

    /** Genera el informe de inventario en formato PDF. */
    @Operation(summary = "Descargar informe de inventario en PDF",
            description = "El rango de fechas se aplica a los movimientos.")
    @GetMapping(value = "/informes/exportar-pdf", produces = "application/pdf")
    ResponseEntity<byte[]> exportarInformeInventarioPdf(
            @RequestParam(defaultValue = "TODOS") String tipo,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) throws IOException;

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
