package com.uniminuto.clinica.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.uniminuto.clinica.models.ImpuestoRq;
import com.uniminuto.clinica.models.TarifaRq;
import com.uniminuto.clinica.models.TarifaRs;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Expone el catálogo administrativo de tarifas, su historial y la configuración del IVA de
 * medicamentos.
 */
@Tag(name = "Tarifas e IVA", description = "Administración. Configuración de valores de servicios e IVA de medicamentos.")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/tarifa")
public interface TarifaApi {
    /** Consulta el catálogo y permite filtrar por especialidad. */
    @Operation(summary = "Listar tarifas")
    @GetMapping
    List<TarifaRs> listar(@RequestParam(required = false) Long especializacionId);

    /** Registra una tarifa de un servicio con su valor, IVA y fecha de vigencia. */
    @Operation(summary = "Registrar tarifa")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TarifaRs crear(@RequestBody TarifaRq solicitud, Principal usuario);

    /** Edita una tarifa y deja registro de sus cambios. */
    @Operation(summary = "Actualizar tarifa")
    @PutMapping("/{id}")
    TarifaRs actualizar(@PathVariable long id, @RequestBody TarifaRq solicitud, Principal usuario);

    /** Consulta los cambios registrados para la tarifa. */
    @Operation(summary = "Consultar historial de cambios de tarifa")
    @GetMapping("/{id}/historial")
    List<Map<String, Object>> historial(@PathVariable long id);

    /** Guarda el porcentaje de IVA aplicable al medicamento. */
    @Operation(summary = "Configurar IVA de un medicamento")
    @PutMapping("/impuestos-medicamento/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void configurarIva(@PathVariable long id, @RequestBody ImpuestoRq solicitud, Principal usuario);
}
