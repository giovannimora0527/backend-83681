package com.uniminuto.clinica.modulos.atencion;

import com.uniminuto.clinica.support.Http;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping
@Tag(name = "3b. Atención médica y fórmulas", description = "Perfil MEDICO (escritura) y ADMINISTRADOR (consulta). "
        + "La fórmula no descuenta inventario; solo el despacho lo hace.")
public class AtencionController {

    private final AtencionService service;

    public AtencionController(AtencionService service) {
        this.service = service;
    }

    @Operation(summary = "Lista atenciones (el médico solo ve las suyas); filtros estado y q (mascota/propietario)")
    @GetMapping("/atencion")
    public List<Map<String, Object>> listar(@RequestParam(required = false) String estado,
                                            @RequestParam(required = false) String q) {
        return service.listar(estado, q);
    }

    @Operation(summary = "Inicia la atención de una cita (cita pasa a EN_ATENCION y se agrega la consulta base)")
    @PostMapping("/atencion/iniciar/{citaId}")
    public Map<String, Object> iniciar(@PathVariable long citaId) {
        return service.iniciar(citaId);
    }

    @Operation(summary = "Detalle de la atención con servicios, fórmula, totales y antecedentes de la mascota")
    @GetMapping("/atencion/{id}")
    public Map<String, Object> detalle(@PathVariable long id) {
        return service.detalle(id);
    }

    @Operation(summary = "Atención asociada a una cita")
    @GetMapping("/atencion/cita/{citaId}")
    public Map<String, Object> porCita(@PathVariable long citaId) {
        return service.porCita(citaId);
    }

    @Operation(summary = "Guarda motivo, síntomas, diagnóstico, observaciones y recomendaciones")
    @PutMapping("/atencion/{id}")
    public Map<String, Object> guardar(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.guardar(id, body);
    }

    @Operation(summary = "Agrega (o actualiza la cantidad de) un servicio/procedimiento tarifado",
            description = "Cuerpo: {tarifaId, cantidad}. Se copia el valor vigente de la tarifa.")
    @PostMapping("/atencion/{id}/servicios")
    public Map<String, Object> agregarServicio(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.agregarServicio(id, body);
    }

    @Operation(summary = "Quita un servicio de la atención en curso")
    @DeleteMapping("/atencion/{id}/servicios/{servicioId}")
    public Map<String, Object> quitarServicio(@PathVariable long id, @PathVariable long servicioId) {
        return service.quitarServicio(id, servicioId);
    }

    @Operation(summary = "Finaliza la atención: registra la historia clínica, confirma la fórmula y deja la cita ATENDIDA")
    @PostMapping("/atencion/{id}/finalizar")
    public Map<String, Object> finalizar(@PathVariable long id) {
        return service.finalizar(id);
    }

    @Operation(summary = "Prefactura de la atención en PDF")
    @GetMapping("/atencion/{id}/prefactura/pdf")
    public ResponseEntity<byte[]> prefactura(@PathVariable long id) {
        return Http.pdf(service.prefacturaPdf(id), "prefactura-" + id + ".pdf");
    }

    @Operation(summary = "Crea o reemplaza la fórmula médica (varios medicamentos) de la atención",
            description = "Cuerpo: {observaciones, items:[{medicamentoId, dosis, frecuencia, duracionDias, cantidadPrescrita, viaAdministracion, indicaciones}]}")
    @PutMapping("/formulas/atencion/{atencionId}")
    public Map<String, Object> guardarFormula(@PathVariable long atencionId, @RequestBody Map<String, Object> body) {
        return service.guardarFormula(atencionId, body);
    }

    @Operation(summary = "Fórmula médica de la atención en PDF")
    @GetMapping("/formulas/atencion/{atencionId}/pdf")
    public ResponseEntity<byte[]> formulaPdf(@PathVariable long atencionId) {
        return Http.pdf(service.formulaPdf(atencionId), "formula-atencion-" + atencionId + ".pdf");
    }
}
