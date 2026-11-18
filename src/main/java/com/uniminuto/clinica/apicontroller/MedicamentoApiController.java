package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.InventarioLoteRs;
import com.uniminuto.clinica.models.InformeInventarioRs;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.service.MedicamentoService;
import com.uniminuto.clinica.service.InventarioLoteService;
import com.uniminuto.clinica.service.MovimientoInventarioService;
import com.uniminuto.clinica.util.ExportarReporte;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    private final MedicamentoService medicamentoService;
    private final InventarioLoteService inventarioLoteService;
    private final MovimientoInventarioService movimientoInventarioService;

    /** Construye el controlador con los servicios de medicamentos, lotes y movimientos. */
    public MedicamentoApiController(MedicamentoService medicamentoService,
                                    InventarioLoteService inventarioLoteService,
                                    MovimientoInventarioService movimientoInventarioService) {
        this.medicamentoService = medicamentoService;
        this.inventarioLoteService = inventarioLoteService;
        this.movimientoInventarioService = movimientoInventarioService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.listarMedicamentos());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> buscarMedicamentos(String termino) {
        return ResponseEntity.ok(medicamentoService.buscarMedicamentos(termino));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> crearMedicamento(
            MedicamentoRq medicamentoRq, @AuthenticationPrincipal String usuarioResponsableId) {
        return ResponseEntity.ok(medicamentoService.crearMedicamento(medicamentoRq, usuarioResponsableId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedicamento(
            MedicamentoRq medicamentoRq, @AuthenticationPrincipal String usuarioResponsableId) {
        return ResponseEntity.ok(medicamentoService.actualizarMedicamento(medicamentoRq, usuarioResponsableId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarDisponibles() {
        return ResponseEntity.ok(medicamentoService.listarDisponibles());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarAgotados() {
        return ResponseEntity.ok(medicamentoService.listarAgotados());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarStockBajo() {
        return ResponseEntity.ok(medicamentoService.listarStockBajo());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarProximosAVencer(int dias) {
        return ResponseEntity.ok(medicamentoService.listarProximosAVencer(dias));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(Long medicamentoId, Integer cantidadRequerida) {
        return ResponseEntity.ok(medicamentoService.consultarDisponibilidad(medicamentoId, cantidadRequerida));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MedicamentoInventarioRs>> consultarInventarioParaDespacho() {
        return ResponseEntity.ok(medicamentoService.listarMedicamentos().stream()
                .map(this::mapearMedicamentoInventario)
                .toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<InventarioLoteRs>> consultarLotesParaDespacho(Long medicamentoId) {
        return ResponseEntity.ok(inventarioLoteService.listarLotesPorMedicamento(medicamentoId).stream()
                .map(this::mapearLote)
                .toList());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<MovimientoInventarioRs>> consultarMovimientosParaDespacho(Long medicamentoId) {
        return ResponseEntity.ok(movimientoInventarioService.listarPorMedicamento(medicamentoId).stream()
                .map(this::mapearMovimiento)
                .toList());
    }

    /** Descarga un libro Excel con resumen, lotes y movimientos filtrables. */
    @Override
    public ResponseEntity<byte[]> exportarInformeInventarioExcel(String tipo, LocalDate desde, LocalDate hasta) {
        byte[] archivo = medicamentoService.exportarInventarioExcel(tipo, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"informe_inventario.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }

    @Override
    public InformeInventarioRs consultarInformeInventario(String tipo, LocalDate desde, LocalDate hasta) {
        return medicamentoService.consultarInformeInventario(tipo, desde, hasta);
    }

    @Override
    public ResponseEntity<byte[]> exportarInformeInventarioPdf(String tipo, LocalDate desde, LocalDate hasta)
            throws IOException {
        var informe = medicamentoService.consultarInformeInventario(tipo, desde, hasta);
        var filas = new ArrayList<List<Object>>(informe.parametros());
        filas.add(List.of("Totales"));
        filas.addAll(informe.filasTotales());
        for (var seccion : informe.secciones()) {
            filas.add(List.of(seccion.nombre()));
            filas.add(new ArrayList<>(seccion.columnas()));
            seccion.filas().stream()
                    .map(fila -> fila.stream().map(valor -> valor == null ? "" : valor).toList())
                    .forEach(filas::add);
        }
        var reporte = new ReporteRs("Informe de inventario", informe.generado(), List.of(), filas);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"informe_inventario.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(ExportarReporte.pdf(reporte));
    }

    /** Convierte un lote al contrato de consulta utilizado en inventario. */
    private InventarioLoteRs mapearLote(InventarioLote lote) {
        InventarioLoteRs respuesta = new InventarioLoteRs();
        respuesta.setId(lote.getId());
        respuesta.setMedicamentoId(lote.getMedicamento() == null ? null : lote.getMedicamento().getId());
        respuesta.setNumeroLote(lote.getNumeroLote());
        respuesta.setCantidad(lote.getCantidad());
        respuesta.setPrecioCompraLote(lote.getPrecioCompraLote());
        respuesta.setFechaVencimiento(lote.getFechaVencimiento());
        respuesta.setFechaIngreso(lote.getFechaIngreso());
        respuesta.setActivo(lote.getActivo());
        return respuesta;
    }

    /** Convierte un medicamento a un resumen plano para la consulta de despacho. */
    private MedicamentoInventarioRs mapearMedicamentoInventario(Medicamento medicamento) {
        MedicamentoInventarioRs respuesta = new MedicamentoInventarioRs();
        respuesta.setId(medicamento.getId());
        respuesta.setNombre(medicamento.getNombre());
        respuesta.setTipoProducto(medicamento.getTipoProducto());
        respuesta.setPresentacion(medicamento.getPresentacion());
        respuesta.setRegistroInvima(medicamento.getRegistroInvima());
        respuesta.setPrecioCompra(medicamento.getPrecioCompra());
        respuesta.setPrecioVenta(medicamento.getPrecioVenta());
        respuesta.setCantidadActual(medicamento.getCantidadActual());
        respuesta.setStockMinimo(medicamento.getStockMinimo());
        respuesta.setFechaProximoPedido(medicamento.getFechaProximoPedido());
        return respuesta;
    }

    /** Convierte un movimiento a una respuesta sin referencias JPA recursivas. */
    private MovimientoInventarioRs mapearMovimiento(MovimientoInventario movimiento) {
        MovimientoInventarioRs respuesta = new MovimientoInventarioRs();
        respuesta.setId(movimiento.getId());
        respuesta.setMedicamentoId(movimiento.getMedicamento() == null ? null : movimiento.getMedicamento().getId());
        respuesta.setLoteId(movimiento.getLote() == null ? null : movimiento.getLote().getId());
        respuesta.setTipo(movimiento.getTipo());
        respuesta.setCantidad(movimiento.getCantidad());
        respuesta.setFechaMovimiento(movimiento.getFechaMovimiento());
        respuesta.setUsuarioId(movimiento.getUsuario() == null ? null : movimiento.getUsuario().getId());
        respuesta.setUsuarioAutorizacionId(movimiento.getUsuarioAutorizacion() == null
                ? null : movimiento.getUsuarioAutorizacion().getId());
        respuesta.setFormulaId(movimiento.getFormula() == null ? null : movimiento.getFormula().getId());
        respuesta.setDespachoId(movimiento.getDespacho() == null ? null : movimiento.getDespacho().getId());
        respuesta.setDespachoDetalleId(movimiento.getDespachoDetalle() == null
                ? null : movimiento.getDespachoDetalle().getId());
        respuesta.setMovimientoOriginalId(movimiento.getMovimientoOriginal() == null
                ? null : movimiento.getMovimientoOriginal().getId());
        respuesta.setObservacion(movimiento.getObservacion());
        return respuesta;
    }

}
