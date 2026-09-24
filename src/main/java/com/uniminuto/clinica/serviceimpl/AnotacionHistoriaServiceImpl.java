package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;


    @Override
    public MiRespuestaRS crearAnotacionHistoria(AnotacionRq anotacionRq) throws BadRequestException {
        Optional<Mascota> optMascota = mascotaRepository
                .findById(anotacionRq.getMascotaId());
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + anotacionRq.getMascotaId() + " no existe.");
        }

        Optional<Medico> optMedico = medicoRepository
                .findById(anotacionRq.getMedicoId());
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + anotacionRq.getMedicoId() + " no existe.");
        }

        Optional<HistoriaMedica> optHistoria = this.historiaMedicaRepository
                .findByMascota(optMascota.get());
        HistoriaMedica historia = new HistoriaMedica();
        if (optHistoria.isEmpty()) {
            historia.setMascota(optMascota.get());
            historia.setFechaCreacion(LocalDateTime.now());
            historia = this.historiaMedicaRepository.save(historia);
        } else {
            historia = optHistoria.get();
        }

        AnotacionHistoria anotacionNueva = new AnotacionHistoria();
        anotacionNueva.setHistoria(historia);
        anotacionNueva.setDescripcion(anotacionRq.getDescripcion());
        anotacionNueva.setFecha(LocalDateTime.now());
        anotacionNueva.setMedico(optMedico.get());
        this.anotacionHistoriaRepository.save(anotacionNueva);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Anotación creada correctamente para el paciente: "
         + optMascota.get().getNombreMascota());
        return rta;
    }
}
