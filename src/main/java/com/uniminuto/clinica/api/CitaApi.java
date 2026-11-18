package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.models.CitaMedicaRq;
import com.uniminuto.clinica.models.CitaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cita")
public interface CitaApi {

    @GetMapping("/calendario")
    ResponseEntity<List<CitaMedicaRs>> calendario(@RequestParam Long medicoId,
                                                   @RequestParam LocalDateTime fechaInicio,
                                                   @RequestParam LocalDateTime fechaFin);

    @GetMapping("/historial")
    ResponseEntity<List<CitaMedicaRs>> historial(@RequestParam Integer mascotaId);

    @PostMapping
    ResponseEntity<MiRespuestaRS> crearCita(@RequestBody CitaMedicaRq cita);

    @PutMapping("/{id}")
    ResponseEntity<MiRespuestaRS> actualizarCita(@PathVariable Long id,
                                                  @RequestBody CitaMedicaRq cita);

    @PatchMapping("/{id}/estado")
    ResponseEntity<MiRespuestaRS> cambiarEstado(@PathVariable Long id,
                                                @RequestParam EstadoCita estado);

    @GetMapping("/reporte.xlsx")
    ResponseEntity<byte[]> reporteExcel(@RequestParam Long medicoId,
                                        @RequestParam LocalDateTime fechaInicio,
                                        @RequestParam LocalDateTime fechaFin);
}
