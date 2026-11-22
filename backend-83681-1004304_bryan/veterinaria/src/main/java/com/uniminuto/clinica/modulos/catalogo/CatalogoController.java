package com.uniminuto.clinica.modulos.catalogo;

import com.uniminuto.clinica.modulos.citas.CitasService;
import com.uniminuto.clinica.support.In;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/catalogo")
@Tag(name = "3a. Especialidades y tarifas", description = "Módulo administrativo. Lectura: ADMINISTRADOR, RECEPCION, MEDICO. "
        + "Escritura: solo ADMINISTRADOR.")
public class CatalogoController {

    private final CatalogoService service;
    private final CitasService citas;

    public CatalogoController(CatalogoService service, CitasService citas) {
        this.service = service;
        this.citas = citas;
    }

    @Operation(summary = "Lista especialidades veterinarias (activas e inactivas)")
    @GetMapping("/especialidades")
    public List<Map<String, Object>> especialidades(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return service.especialidades(soloActivas);
    }

    @Operation(summary = "Registra una especialidad (nombre, codigoEspecializacion, descripcion)")
    @PostMapping("/especialidades")
    public Map<String, Object> crearEspecialidad(@RequestBody Map<String, Object> body) {
        return service.crearEspecialidad(body);
    }

    @Operation(summary = "Edita una especialidad")
    @PutMapping("/especialidades/{id}")
    public Map<String, Object> editarEspecialidad(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.editarEspecialidad(id, body);
    }

    @Operation(summary = "Activa o inactiva una especialidad")
    @PatchMapping("/especialidades/{id}/estado")
    public Map<String, Object> estadoEspecialidad(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.estadoEspecialidad(id, In.bool(body, "activo", true));
    }

    @Operation(summary = "Lista tarifas por tipo de consulta o servicio, con filtros")
    @GetMapping("/tarifas")
    public List<Map<String, Object>> tarifas(@RequestParam(required = false) String tipo,
                                             @RequestParam(required = false) Boolean activo,
                                             @RequestParam(required = false) Long especialidadId) {
        return service.tarifas(tipo, activo, especialidadId);
    }

    @Operation(summary = "Registra una tarifa",
            description = "Campos: codigo, nombre, tipo (CONSULTA | PROCEDIMIENTO | SERVICIO), especializacionId (opcional), valor, ivaPorcentaje.")
    @PostMapping("/tarifas")
    public Map<String, Object> crearTarifa(@RequestBody Map<String, Object> body) {
        return service.crearTarifa(body);
    }

    @Operation(summary = "Edita una tarifa; si cambia el valor o el IVA queda registrado en el log de tarifas")
    @PutMapping("/tarifas/{id}")
    public Map<String, Object> editarTarifa(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.editarTarifa(id, body);
    }

    @Operation(summary = "Activa o inactiva una tarifa")
    @PatchMapping("/tarifas/{id}/estado")
    public Map<String, Object> estadoTarifa(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.estadoTarifa(id, In.bool(body, "activo", true));
    }

    @Operation(summary = "Historial (log) de cambios de tarifas; tarifaId opcional")
    @GetMapping("/tarifas-log")
    public List<Map<String, Object>> log(@RequestParam(required = false) Long tarifaId) {
        return service.logTarifas(tarifaId);
    }

    @Operation(summary = "Horario de disponibilidad de un veterinario")
    @GetMapping("/medicos/{medicoId}/horarios")
    public List<Map<String, Object>> horarios(@PathVariable long medicoId) {
        return citas.horariosDe(medicoId);
    }

    @Operation(summary = "Reemplaza el horario de disponibilidad de un veterinario",
            description = "Cuerpo: lista de bloques {diaSemana (1=lunes..7=domingo), horaInicio (HH:mm), horaFin (HH:mm), activo}.")
    @PutMapping("/medicos/{medicoId}/horarios")
    public List<Map<String, Object>> guardarHorarios(@PathVariable long medicoId, @RequestBody List<Map<String, Object>> bloques) {
        return citas.guardarHorarios(medicoId, bloques);
    }
}
