package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.IngresoLoteRq;
import com.uniminuto.clinica.models.InventarioItemRs;
import com.uniminuto.clinica.models.LoteInventarioRs;
import com.uniminuto.clinica.models.MovimientoInventarioRs;
import com.uniminuto.clinica.models.ImportacionInventarioRs;
import com.uniminuto.clinica.models.DespachoPendienteRs;
import com.uniminuto.clinica.models.ComprobanteDespachoRs;
import com.uniminuto.clinica.models.DespacharFormulaRq;
import com.uniminuto.clinica.models.DespachoRs;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/inventario")
public interface InventarioApi {

    @GetMapping(value = "/resumen", produces = "application/json")
    ResponseEntity<List<InventarioItemRs>> obtenerResumen(
            @RequestParam(name = "estado", required = false) String estado);

    @GetMapping(value = "/medicamentos/{medicamentoId}/lotes", produces = "application/json")
    ResponseEntity<List<LoteInventarioRs>> obtenerLotes(@PathVariable("medicamentoId") Long medicamentoId)
            throws BadRequestException;

    @GetMapping(value = "/medicamentos/{medicamentoId}/movimientos", produces = "application/json")
    ResponseEntity<List<MovimientoInventarioRs>> obtenerMovimientos(
            @PathVariable("medicamentoId") Long medicamentoId)
            throws BadRequestException;

    @PostMapping(value = "/lotes", produces = "application/json", consumes = "application/json")
    ResponseEntity<LoteInventarioRs> registrarIngreso(@RequestBody IngresoLoteRq request)
            throws BadRequestException;

    @PostMapping(value = "/importaciones/previsualizar", consumes = "multipart/form-data", produces = "application/json")
    ResponseEntity<ImportacionInventarioRs> previsualizarImportacion(@RequestPart("archivo") MultipartFile archivo);

    @PostMapping(value = "/importaciones/ejecutar", consumes = "multipart/form-data", produces = "application/json")
    ResponseEntity<ImportacionInventarioRs> ejecutarImportacion(@RequestPart("archivo") MultipartFile archivo);

    @GetMapping(value = "/despachos/pendientes", produces = "application/json")
    ResponseEntity<List<DespachoPendienteRs>> obtenerDespachosPendientes();

    @GetMapping(value = "/despachos/{formulaId}", produces = "application/json")
    ResponseEntity<ComprobanteDespachoRs> obtenerComprobanteDespacho(@PathVariable("formulaId") Long formulaId);

    @GetMapping(value = "/despachos/{formulaId}.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<byte[]> descargarComprobanteDespacho(@PathVariable("formulaId") Long formulaId);

    @PostMapping(value = "/despachos/{formulaId}", consumes = "application/json", produces = "application/json")
    ResponseEntity<DespachoRs> despacharParcial(@PathVariable("formulaId") Long formulaId,
            @RequestBody DespacharFormulaRq request);
}
