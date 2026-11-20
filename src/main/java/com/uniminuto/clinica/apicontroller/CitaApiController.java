package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.models.DisponibilidadMedicoRq;
import com.uniminuto.clinica.entity.CitaDisponibilidad;
import com.uniminuto.clinica.serviceimpl.CitaServiceImpl;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MedicoUsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;
    @Autowired private CitaServiceImpl citaServiceImpl;
    @Autowired private CitaRepository citaRepository;
    @Autowired private MedicoUsuarioRepository medicoUsuarioRepository;


    @Override
    public ResponseEntity<List<Cita>> listarCitas() throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitas());
    }

    @Override
    public ResponseEntity<List<Cita>> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitasRango(fechaInicio, fechaFin));
    }

    @Override
    public ResponseEntity<List<Cita>> listarCitasPorMascota(Integer mascotaId) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitasPorMascota(mascotaId));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearCita(CitaRq cita) throws BadRequestException {
        validarPropiedadCita(cita == null ? null : cita.getMedicoId(), null);
        return ResponseEntity.ok(this.citaService.crearCita(cita));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq cita) throws BadRequestException {
        Long actual = cita == null || cita.getId() == null ? null : citaRepository.findById(cita.getId())
                .map(c -> c.getMedico().getId()).orElse(null);
        validarPropiedadCita(cita == null ? null : cita.getMedicoId(), actual);
        return ResponseEntity.ok(this.citaService.actualizarCita(cita));
    }

    @Override public ResponseEntity<List<Cita>> listarMisCitas(LocalDateTime inicio, LocalDateTime fin, HttpServletRequest request) {
        return ResponseEntity.ok(citaService.listarCitasMedico(usuarioAutenticado(request), inicio, fin));
    }

    @Override public ResponseEntity<CitaDisponibilidad> crearDisponibilidad(DisponibilidadMedicoRq disponibilidad) {
        return ResponseEntity.status(201).body(citaServiceImpl.crearDisponibilidad(disponibilidad));
    }

    @Override public ResponseEntity<List<CitaDisponibilidad>> disponibilidadMedico(Long medicoId) {
        HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
        if ("MEDICO".equals(request.getAttribute("authenticatedRole"))) validarPropiedadCita(medicoId, null);
        return ResponseEntity.ok(citaServiceImpl.disponibilidadMedico(medicoId));
    }

    private void validarPropiedadCita(Long medicoSolicitado, Long medicoExistente) {
        HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest();
        if (!"MEDICO".equals(request.getAttribute("authenticatedRole"))) return;
        Long asignado = medicoUsuarioRepository.findByUsuarioId(usuarioAutenticado(request)).map(m -> m.getMedicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no está vinculado a un médico."));
        if (!asignado.equals(medicoSolicitado) || (medicoExistente != null && !asignado.equals(medicoExistente)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El veterinario solo puede gestionar sus propias citas.");
    }
    private static Long usuarioAutenticado(HttpServletRequest request) {
        Object id = request.getAttribute("authenticatedUserId");
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin identificador de usuario.");
        return Long.valueOf(id.toString());
    }
}
