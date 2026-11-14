package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.DespachoCancelarRq;
import com.uniminuto.clinica.models.DespachoDetalleRs;
import com.uniminuto.clinica.models.DespachoHistorialEstadoRs;
import com.uniminuto.clinica.models.DespachoRq;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/** API para gestionar la dispensación de fórmulas médicas. */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("dispensaciones")
public interface DespachoApi {

    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<DespachoRs>> listarDespachos()
            throws BadRequestException;

    @GetMapping(value = "/buscar/{despachoId}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<DespachoRs> consultarDespacho(
            @PathVariable("despachoId") Long despachoId)
            throws BadRequestException;

    @GetMapping(value = "/formula/{formulaId}",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<DespachoRs>> listarDespachosPorFormula(
            @PathVariable("formulaId") Long formulaId)
            throws BadRequestException;

    @GetMapping(value = "/{despachoId}/detalles",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<DespachoDetalleRs>> listarDetalles(
            @PathVariable("despachoId") Long despachoId)
            throws BadRequestException;

    @GetMapping(value = "/{despachoId}/historial",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<DespachoHistorialEstadoRs>> listarHistorial(
            @PathVariable("despachoId") Long despachoId)
            throws BadRequestException;

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<DespachoRs> guardarDespacho(
            @RequestBody DespachoRq despachoRq,
            @AuthenticationPrincipal String usuarioResponsableId)
            throws BadRequestException;

    @PostMapping(value = "/{despachoId}/cancelar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> cancelarDespacho(
            @PathVariable("despachoId") Long despachoId,
            @RequestBody DespachoCancelarRq cancelacionRq,
            @AuthenticationPrincipal String usuarioResponsableId)
            throws BadRequestException;
}
