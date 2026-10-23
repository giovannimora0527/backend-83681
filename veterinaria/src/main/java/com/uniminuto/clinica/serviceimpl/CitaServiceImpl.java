package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRequest;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String CITA_PROGRAMADA = "PROGRAMADA";

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public List<CitaMedica> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFinal) {
        return citaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicio, fechaFinal);
    }

    @Override
    public MiRespuestaRS crearCita(CitaRequest citaRq) throws BadRequestException {
        this.validarObjetoEntrada(citaRq);
        this.validarObjetosMascotaYMedico(citaRq);

        List<CitaMedica> citasExistentes = citaRepository.findByMascotaAndMedicoAndFechaHoraBetweenOrderByFechaHoraDesc(
                mascotaRepository.findById(citaRq.getMascotaId()).get(),
                medicoRepository.findById(citaRq.getMedicoId()).get(),
                citaRq.getFechaHora().minusMinutes(10),
                citaRq.getFechaHora().plusMinutes(10)
        );
        if (!citasExistentes.isEmpty()) {
            throw new BadRequestException("Ya existe una cita para la mascota y " +
                    "el médico en el rango de tiempo especificado.");
        }

        List<CitaMedica> citasMedico = citaRepository.findByMedicoAndFechaHoraBetweenOrderByFechaHoraDesc(
                medicoRepository.findById(citaRq.getMedicoId()).get(),
                citaRq.getFechaHora().minusMinutes(10),
                citaRq.getFechaHora().plusMinutes(10)
        );

        if (!citasMedico.isEmpty()) {
            throw new BadRequestException("El médico ya tiene una cita en el rango de tiempo especificado.");
        }

        CitaMedica citaNueva = new CitaMedica();
        Optional<Mascota> optMascota = mascotaRepository
                .findById(citaRq.getMascotaId());
        citaNueva.setMascota(optMascota.get());
        Optional<Medico> optMedico = medicoRepository
                .findById(citaRq.getMedicoId());
        citaNueva.setMedico(optMedico.get());
        citaNueva.setFechaHora(citaRq.getFechaHora());
        citaNueva.setEstado(CITA_PROGRAMADA);
        citaNueva.setMotivo(citaRq.getMotivo());
        this.citaRepository.save(citaNueva);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Cita creada exitosamente.");
        return rta;
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRequest citaRq) throws BadRequestException {
        this.validarObjetoEntrada(citaRq);
        this.validarObjetosMascotaYMedico(citaRq);


        
        return null;
    }

    private void validarObjetoEntrada(CitaRequest citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto citaRq no puede ser nulo.");
        }
        if (citaRq.getMascotaId() == null || citaRq.getMedicoId() == null || citaRq.getFechaHora() == null) {
            throw new BadRequestException("Los campos mascotaId, medicoId y fechaHora son obligatorios.");
        }

    }

    private void validarObjetosMascotaYMedico(CitaRequest citaRq) {
        Optional<Mascota> optMascota = mascotaRepository
                .findById(citaRq.getMascotaId());
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + citaRq.getMascotaId() + " no existe.");
        }

        Optional<Medico> optMedico = medicoRepository
                .findById(citaRq.getMedicoId());
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + citaRq.getMedicoId() + " no existe.");
        }
    }
}
