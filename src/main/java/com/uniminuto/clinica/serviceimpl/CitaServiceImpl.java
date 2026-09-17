// serviceimpl/CitaServiceImpl.java
package com.uniminuto.clinica.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public List<Cita> filtrarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("La fecha inicial y la fecha final son obligatorias");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return citaRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }

    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException {
        this.validar(citaRq);

        Mascota mascota = this.obtenerMascota(citaRq.getMascotaId());
        Medico medico = this.obtenerMedico(citaRq.getMedicoId());

        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFecha(citaRq.getFecha());
        cita.setMotivo(citaRq.getMotivo());
        cita.setObservaciones(citaRq.getObservaciones());

        citaRepository.save(cita);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        this.validar(citaRq);

        if (citaRq.getId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio para actualizar");
        }

        Optional<Cita> optCita = citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + citaRq.getId() + " no existe");
        }

        Mascota mascota = this.obtenerMascota(citaRq.getMascotaId());
        Medico medico = this.obtenerMedico(citaRq.getMedicoId());

        Cita cita = optCita.get();
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFecha(citaRq.getFecha());
        cita.setMotivo(citaRq.getMotivo());
        cita.setObservaciones(citaRq.getObservaciones());

        citaRepository.save(cita);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita actualizada correctamente");
        return respuesta;
    }

    private void validar(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (citaRq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota es obligatorio");
        }
        if (citaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (citaRq.getFecha() == null) {
            throw new BadRequestException("La fecha de la cita es obligatoria");
        }
        if (citaRq.getMotivo() == null || citaRq.getMotivo().trim().isEmpty()) {
            throw new BadRequestException("El motivo de la cita no puede estar vacío");
        }
    }

    private Mascota obtenerMascota(Long id) throws BadRequestException {
        Optional<Mascota> optMascota = mascotaRepository.findById(id);
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + id + " no existe");
        }
        return optMascota.get();
    }

    private Medico obtenerMedico(Long id) throws BadRequestException {
        Optional<Medico> optMedico = medicoRepository.findById(id);
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + id + " no existe");
        }
        return optMedico.get();
    }
}