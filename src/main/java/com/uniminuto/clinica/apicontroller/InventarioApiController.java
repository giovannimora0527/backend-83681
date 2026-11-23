package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.InventarioApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.IngresoLoteRq;
import com.uniminuto.clinica.models.InventarioItemRs;
import com.uniminuto.clinica.models.LoteInventarioRs;
import com.uniminuto.clinica.models.MovimientoInventarioRs;
import com.uniminuto.clinica.models.ImportacionInventarioRs;
import com.uniminuto.clinica.models.DespachoPendienteRs;
import com.uniminuto.clinica.models.ComprobanteDespachoRs;
import com.uniminuto.clinica.service.InventarioService;
import com.uniminuto.clinica.serviceimpl.ImportacionInventarioService;
import com.uniminuto.clinica.service.FormulaService;
import com.uniminuto.clinica.models.DespacharFormulaRq;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.util.PdfUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InventarioApiController implements InventarioApi {

    private final InventarioService inventarioService;
    private final ImportacionInventarioService importacionService;
    private final FormulaService formulaService;

    public InventarioApiController(InventarioService inventarioService,
            ImportacionInventarioService importacionService, FormulaService formulaService) {
        this.inventarioService = inventarioService;
        this.importacionService = importacionService;
        this.formulaService = formulaService;
    }

    @Override
    public ResponseEntity<List<InventarioItemRs>> obtenerResumen(String estado) {
        return ResponseEntity.ok(inventarioService.obtenerResumen(estado));
    }

    @Override
    public ResponseEntity<List<LoteInventarioRs>> obtenerLotes(Long medicamentoId) throws BadRequestException {
        return ResponseEntity.ok(inventarioService.obtenerLotes(medicamentoId));
    }

    @Override
    public ResponseEntity<List<MovimientoInventarioRs>> obtenerMovimientos(Long medicamentoId)
            throws BadRequestException {
        return ResponseEntity.ok(inventarioService.obtenerMovimientos(medicamentoId));
    }

    @Override
    public ResponseEntity<LoteInventarioRs> registrarIngreso(IngresoLoteRq request) throws BadRequestException {
        return ResponseEntity.status(201).body(inventarioService.registrarIngreso(request));
    }

    @Override
    public ResponseEntity<ImportacionInventarioRs> previsualizarImportacion(org.springframework.web.multipart.MultipartFile archivo) {
        return ResponseEntity.ok(importacionService.previsualizar(archivo));
    }

    @Override
    public ResponseEntity<ImportacionInventarioRs> ejecutarImportacion(org.springframework.web.multipart.MultipartFile archivo) {
        return ResponseEntity.status(201).body(importacionService.ejecutar(archivo));
    }

    @Override
    public ResponseEntity<List<DespachoPendienteRs>> obtenerDespachosPendientes() {
        return ResponseEntity.ok(inventarioService.obtenerDespachosPendientes());
    }

    @Override
    public ResponseEntity<ComprobanteDespachoRs> obtenerComprobanteDespacho(Long formulaId) {
        return ResponseEntity.ok(inventarioService.obtenerComprobanteDespacho(formulaId));
    }

    @Override
    public ResponseEntity<byte[]> descargarComprobanteDespacho(Long formulaId) {
        ComprobanteDespachoRs comprobante = inventarioService.obtenerComprobanteDespacho(formulaId);
        List<String> lineas = new java.util.ArrayList<>();
        lineas.add("Formula: " + comprobante.getFormulaId());
        lineas.add("Medicamento: " + comprobante.getMedicamento());
        lineas.add("Estado: " + comprobante.getEstado());
        lineas.add("Cantidad prescrita: " + comprobante.getCantidadPrescrita());
        lineas.add("Cantidad dispensada: " + comprobante.getCantidadDespachada());
        lineas.add("Fecha de entrega: " + comprobante.getFechaDespacho());
        lineas.add("Lotes y movimientos:");
        comprobante.getLotes().forEach(movimiento -> lineas.add(
                movimiento.getFecha() + " | lote " + movimiento.getCodigoLote()
                        + " | cantidad " + movimiento.getCantidad()
                        + " | usuario " + movimiento.getUsuario()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"comprobante-despacho-" + formulaId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(PdfUtil.generar("Comprobante de despacho medico", lineas));
    }

    @Override
    public ResponseEntity<DespachoRs> despacharParcial(Long formulaId, DespacharFormulaRq request) {
        return ResponseEntity.ok(formulaService.dispensarParcial(formulaId, request));
    }
}
