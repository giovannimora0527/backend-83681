package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gestion")
@CrossOrigin(origins = "*")
public class GestionClinicaController {
    private final CitaRepository citas;
    private final ClienteRepository clientes;
    private final MascotaRepository mascotas;
    private final MedicoRepository medicos;
    private final HistoriaMedicaRepository historias;
    private final AnotacionHistoriaRepository anotaciones;
    private final FormulaMedicaRepository formulas;
    private final MedicamentoRepository medicamentos;

    public GestionClinicaController(CitaRepository citas, ClienteRepository clientes,
            MascotaRepository mascotas, MedicoRepository medicos, HistoriaMedicaRepository historias,
            AnotacionHistoriaRepository anotaciones, FormulaMedicaRepository formulas,
            MedicamentoRepository medicamentos) {
        this.citas = citas; this.clientes = clientes; this.mascotas = mascotas; this.medicos = medicos;
        this.historias = historias; this.anotaciones = anotaciones; this.formulas = formulas; this.medicamentos = medicamentos;
    }

    @GetMapping("/clientes")
    public List<Cliente> clientes() { return clientes.findAll(); }

    @GetMapping("/mascotas")
    public List<Mascota> mascotas(@RequestParam(required = false) Long clienteId) {
        List<Mascota> todos = mascotas.findAll();
        return clienteId == null ? todos : todos.stream().filter(m -> m.getCliente() != null && clienteId.equals(m.getCliente().getId())).toList();
    }

    @GetMapping("/medicos")
    public List<Medico> medicos() { return medicos.findAll(); }

    @GetMapping("/medicamentos")
    public List<Medicamento> medicamentos() { return medicamentos.findAll(); }

    @GetMapping("/citas")
    public List<CitaMedica> citas() { return citas.findAll().stream().sorted((a,b) -> b.getFechaHora().compareTo(a.getFechaHora())).toList(); }

    @GetMapping("/citas/{id}")
    public CitaMedica cita(@PathVariable Long id) { return citas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La cita no existe.")); }

    @PostMapping("/citas")
    @Transactional
    public ResponseEntity<CitaMedica> crearCita(@RequestBody Map<String, Object> body) {
        Long mascotaId = numero(body, "mascotaId"); Long medicoId = numero(body, "medicoId");
        LocalDateTime fecha = fecha(body, "fechaHora"); validarCita(mascotaId, medicoId, fecha, null);
        Mascota mascota = mascotas.findById(mascotaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota no existe."));
        if (mascota.getCliente() == null || !mascota.getCliente().getId().equals(numero(body, "clienteId"))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota no pertenece al cliente seleccionado.");
        Medico medico = medicos.findById(medicoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario no existe."));
        CitaMedica c = new CitaMedica(); c.setMascota(mascota); c.setMedico(medico); c.setClienteId(mascota.getCliente().getId().intValue());
        String estado = String.valueOf(body.getOrDefault("estado", "PENDIENTE")).toUpperCase(); validarEstado(estado);
        c.setFechaHora(fecha); c.setMotivo(String.valueOf(body.getOrDefault("motivo", ""))); c.setEstado(estado);
        return ResponseEntity.status(HttpStatus.CREATED).body(citas.save(c));
    }

    @PutMapping("/citas/{id}")
    @Transactional
    public CitaMedica actualizarCita(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        CitaMedica c = citas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La cita no existe."));
        if ("CANCELADA".equalsIgnoreCase(c.getEstado())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Una cita cancelada no se puede modificar.");
        if ("ATENDIDA".equalsIgnoreCase(c.getEstado())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Una cita atendida conserva su información histórica.");
        Long mascotaId = numero(body, "mascotaId"); Long medicoId = numero(body, "medicoId"); LocalDateTime fecha = fecha(body, "fechaHora"); validarCita(mascotaId, medicoId, fecha, id);
        Mascota m = mascotas.findById(mascotaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota no existe."));
        if (m.getCliente() == null || !m.getCliente().getId().equals(numero(body, "clienteId"))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota no pertenece al cliente seleccionado.");
        c.setMascota(m); c.setClienteId(m.getCliente().getId().intValue()); c.setMedico(medicos.findById(medicoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario no existe.")));
        String estado = String.valueOf(body.getOrDefault("estado", c.getEstado())).toUpperCase(); validarEstado(estado);
        c.setFechaHora(fecha); c.setMotivo(String.valueOf(body.getOrDefault("motivo", c.getMotivo()))); c.setEstado(estado); return citas.save(c);
    }

    private void validarEstado(String estado) {
        if (!List.of("PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA", "PROGRAMADA").contains(estado)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado de la cita no es válido.");
    }

    private void validarCita(Long mascotaId, Long medicoId, LocalDateTime fecha, Long ignorarId) {
        if (fecha.isBefore(LocalDateTime.now().withSecond(0).withNano(0))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden registrar citas en el pasado.");
        Mascota m = mascotas.findById(mascotaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota no existe."));
        Medico v = medicos.findById(medicoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario no existe."));
        boolean ocupado = citas.findAll().stream().filter(c -> ignorarId == null || !ignorarId.equals(c.getId()))
          .filter(c -> !"CANCELADA".equalsIgnoreCase(c.getEstado()))
          .anyMatch(c -> Math.abs(java.time.Duration.between(fecha, c.getFechaHora()).toMinutes()) <= 15 &&
             (c.getMedico().getId().equals(v.getId()) || c.getMascota().getMascotaId().equals(m.getMascotaId())));
        if (ocupado) throw new ResponseStatusException(HttpStatus.CONFLICT, "El veterinario o la mascota ya tiene una cita dentro de los 15 minutos de ese horario.");
    }

    @GetMapping("/historias")
    public List<HistoriaMedica> historias() { return historias.findAll().stream().sorted((a,b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion())).toList(); }

    @PostMapping("/historias")
    @Transactional
    public ResponseEntity<HistoriaMedica> crearHistoria(@RequestBody Map<String, Object> body) {
        Long mascotaId = numero(body, "mascotaId"); Mascota m = mascotas.findById(mascotaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota no existe."));
        if (m.getCliente() == null || !m.getCliente().getId().equals(numero(body, "clienteId"))) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La mascota no pertenece al cliente seleccionado.");
        Long citaId = numero(body, "citaId");
        CitaMedica cita = citas.findById(citaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita indicada no existe."));
        if (!cita.getMascota().getMascotaId().equals(mascotaId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita no corresponde a la mascota seleccionada.");
        String motivo = String.valueOf(body.getOrDefault("motivo", "")).trim();
        String diagnostico = String.valueOf(body.getOrDefault("diagnostico", "")).trim();
        String observaciones = String.valueOf(body.getOrDefault("observaciones", "")).trim();
        if (motivo.isBlank() || diagnostico.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El motivo y el diagnóstico son obligatorios.");
        if (!"ATENDIDA".equalsIgnoreCase(cita.getEstado())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede crear una historia clínica para una cita atendida.");
        HistoriaMedica h = new HistoriaMedica(); h.setMascota(m); h.setCita(cita); h.setMedico(cita.getMedico()); h.setMotivo(motivo); h.setDiagnostico(diagnostico); h.setObservaciones(observaciones); h.setFechaCreacion(LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(historias.save(h));
    }

    @GetMapping("/historias/{id}/anotaciones")
    public List<AnotacionHistoria> anotaciones(@PathVariable Long id) {
        HistoriaMedica h = historias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La historia clínica no existe."));
        return anotaciones.findAll().stream().filter(a -> a.getHistoria().getId().equals(h.getId())).sorted((a,b) -> b.getFecha().compareTo(a.getFecha())).toList();
    }

    @PostMapping("/historias/{id}/anotaciones")
    @Transactional
    public ResponseEntity<AnotacionHistoria> crearAnotacion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        HistoriaMedica h = historias.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La historia clínica no existe."));
        Long medicoId = numero(body, "medicoId"); Medico medico = medicos.findById(medicoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El veterinario no existe."));
        String descripcion = String.valueOf(body.getOrDefault("descripcion", "")).trim();
        String diagnostico = String.valueOf(body.getOrDefault("diagnostico", "")).trim();
        String observacion = String.valueOf(body.getOrDefault("observacion", "")).trim();
        if (descripcion.isBlank() || diagnostico.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción y el diagnóstico de la anotación son obligatorios.");
        AnotacionHistoria a = new AnotacionHistoria(); a.setHistoria(h); a.setMedico(medico); a.setFecha(LocalDateTime.now()); a.setDescripcion(descripcion); a.setDiagnostico(diagnostico); a.setObservacion(observacion); return ResponseEntity.status(HttpStatus.CREATED).body(anotaciones.save(a));
    }

    @GetMapping("/anotaciones")
    public List<AnotacionHistoria> todasAnotaciones() { return anotaciones.findAll().stream().sorted((a,b) -> b.getFecha().compareTo(a.getFecha())).toList(); }

    private Long numero(Map<String, Object> body, String campo) {
        try { Object v = body.get(campo); if (v == null) throw new NumberFormatException(); return Long.valueOf(v.toString()); }
        catch (NumberFormatException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo " + campo + " es obligatorio y debe ser válido."); }
    }
    private LocalDateTime fecha(Map<String, Object> body, String campo) {
        try { return LocalDateTime.parse(String.valueOf(body.get(campo))); }
        catch (Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora de la cita son obligatorias."); }
    }
}
