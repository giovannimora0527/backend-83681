package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.modulos.citas.CitasService;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Módulo de citas para ADMINISTRADOR y RECEPCION (el que ya existía). La lectura sigue usando JPA;
 * la escritura delega en {@link CitasService} para aplicar exactamente las mismas reglas de agenda
 * que el calendario del médico (sin traslapes, médico activo, horario disponible, historial).
 */
@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitasService citasService;

    @Override
    public List<Cita> listarCitas() throws BadRequestException {
        return citaRepository.findAll();
    }

    @Override
    public List<Cita> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas no pueden ser nulas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio no puede ser mayor a la fecha de fin");
        }
        List<Cita> citas = citaRepository.findByFechaHoraBetween(fechaInicio, fechaFin);
        citas.sort((c1, c2) -> c1.getFechaHora().compareTo(c2.getFechaHora()));
        return citas;
    }

    @Override
    public List<Cita> listarCitasPorMascota(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio y debe ser mayor a cero.");
        }
        Optional<Mascota> mascota = mascotaRepository.findById(mascotaId);
        if (mascota.isEmpty()) {
            throw new BadRequestException("La mascota no existe");
        }
        return citaRepository.findByMascotaOrderByFechaHoraDesc(mascota.get());
    }

    @Override
    @Transactional
    public MiRespuestaRS crearCita(CitaRq rq) throws BadRequestException {
        this.validar(rq);
        Map<String, Object> body = new HashMap<>();
        body.put("mascotaId", rq.getMascotaId());
        body.put("medicoId", rq.getMedicoId());
        body.put("fechaHora", rq.getFechaHora().toString());
        body.put("motivo", rq.getMotivo());
        Map<String, Object> cita = citasService.crear(body);
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Se agendó una nueva cita para la mascota " + cita.get("nombreMascota")
                + " con el médico " + cita.get("medicoNombre") + " para el día " + rq.getFechaHora());
        return rta;
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarCita(CitaRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El id de la cita es obligatorio.");
        }
        String estado = rq.getEstado() == null ? "" : rq.getEstado().trim().toUpperCase(Locale.ROOT);
        MiRespuestaRS rta = new MiRespuestaRS();
        if (estado.equals("CANCELADA")) {
            citasService.cancelar(rq.getId(), "Cancelada desde el módulo de citas (administración/recepción)");
            rta.setMessage("La cita fue cancelada. Se conserva en el historial.");
            return rta;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("mascotaId", rq.getMascotaId());
        body.put("medicoId", rq.getMedicoId());
        body.put("fechaHora", rq.getFechaHora().toString());
        body.put("motivo", rq.getMotivo());
        citasService.editar(rq.getId(), body);
        if (estado.equals("CONFIRMADA")) {
            Map<String, Object> actual = citasService.obtener(rq.getId());
            if ("PENDIENTE".equals(actual.get("estado"))) {
                citasService.confirmar(rq.getId());
            }
        }
        rta.setMessage("La cita fue actualizada correctamente.");
        return rta;
    }

    private void validar(CitaRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("La cita no puede ser nula");
        }
        if (rq.getMascotaId() == null || rq.getMedicoId() == null) {
            throw new BadRequestException("La mascota y el médico son obligatorios");
        }
        if (rq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede ser nula");
        }
    }
}
