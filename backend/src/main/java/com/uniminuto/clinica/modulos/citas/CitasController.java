package com.uniminuto.clinica.modulos.citas;

import com.uniminuto.clinica.support.Http;
import com.uniminuto.clinica.support.In;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/citas-medicas")
@Tag(name = "2. Calendario del médico", description = "Citas del veterinario autenticado en vista de calendario. "
        + "Lectura: MEDICO y ADMINISTRADOR. Escritura: MEDICO (solo sobre sus propias citas).")
public class CitasController {

    private final CitasService service;

    public CitasController(CitasService service) {
        this.service = service;
    }

    @Operation(summary = "Datos del veterinario asociado al usuario autenticado")
    @GetMapping("/mi-medico")
    public Map<String, Object> miMedico() {
        return service.miMedico();
    }

    @Operation(summary = "Eventos del calendario en un rango de fechas (día, semana o mes)",
            description = "inicio y fin: AAAA-MM-DD. Un MEDICO solo ve sus citas; ADMINISTRADOR debe indicar medicoId.")
    @GetMapping("/calendario")
    public List<Map<String, Object>> calendario(@RequestParam String inicio, @RequestParam String fin,
                                                @RequestParam(required = false) Long medicoId) {
        return service.calendario(In.parseDate(inicio, "inicio"), In.parseDate(fin, "fin"), medicoId);
    }

    @Operation(summary = "Horario de disponibilidad del veterinario (bloques por día de la semana)")
    @GetMapping("/horarios")
    public List<Map<String, Object>> horarios(@RequestParam(required = false) Long medicoId) {
        return service.horarios(medicoId);
    }

    @Operation(summary = "Detalle de una cita: mascota, propietario, motivo, inicio y fin")
    @GetMapping("/{id}")
    public Map<String, Object> obtener(@PathVariable long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Registra una cita",
            description = "Campos: mascotaId, fechaHora (AAAA-MM-DDTHH:mm), duracionMin (opcional, 30 por defecto), motivo. "
                    + "Valida: médico activo, horario disponible, sin traslapes del médico ni de la mascota.")
    @PostMapping
    public Map<String, Object> crear(@RequestBody Map<String, Object> body) {
        return service.crear(body);
    }

    @Operation(summary = "Edita o reprograma una cita pendiente/confirmada (cambio de fecha y hora, duración o motivo)")
    @PutMapping("/{id}")
    public Map<String, Object> editar(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.editar(id, body);
    }

    @Operation(summary = "Confirma una cita pendiente")
    @PostMapping("/{id}/confirmar")
    public Map<String, Object> confirmar(@PathVariable long id) {
        return service.confirmar(id);
    }

    @Operation(summary = "Cancela una cita (conserva el historial con motivo, fecha y usuario)")
    @PostMapping("/{id}/cancelar")
    public Map<String, Object> cancelar(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.cancelar(id, In.str(body, "motivo"));
    }

    @Operation(summary = "Marca la cita como atendida")
    @PostMapping("/{id}/atendida")
    public Map<String, Object> atendida(@PathVariable long id) {
        return service.marcarAtendida(id);
    }

    @Operation(summary = "Veterinarios activos (para la vista del administrador)")
    @GetMapping("/medicos")
    public List<Map<String, Object>> medicos() {
        return service.medicosParaAgenda();
    }

    @Operation(summary = "Mascotas activas con su propietario, para agendar citas")
    @GetMapping("/mascotas")
    public List<Map<String, Object>> mascotas() {
        return service.mascotasParaAgenda();
    }

    @Operation(summary = "Historial de citas de una mascota (incluye las canceladas)")
    @GetMapping("/mascota/{mascotaId}")
    public List<Map<String, Object>> porMascota(@PathVariable int mascotaId) {
        return service.porMascota(mascotaId);
    }

    @Operation(summary = "Reporte Excel (.xlsx) de las citas del médico con filtro de fecha inicio y fin")
    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> reporte(@RequestParam String inicio, @RequestParam String fin,
                                          @RequestParam(required = false) Long medicoId) {
        LocalDate i = In.parseDate(inicio, "inicio");
        LocalDate f = In.parseDate(fin, "fin");
        return Http.xlsx(service.reporteExcel(i, f, medicoId), "citas_" + i + "_" + f + ".xlsx");
    }
}
