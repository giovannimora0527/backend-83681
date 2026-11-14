package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
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

/** API REST para administrar y consultar el inventario de medicamentos y productos médicos. */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medicamento")
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

}
