package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion del servicio de formulas medicas.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormulaMedica> listar() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public MiRespuestaRS guardar(FormulaMedicaRq rq) throws BadRequestException {
        this.validar(rq);
        Mascota mascota = this.obtenerMascota(rq.getMascotaId());
        Medico medico = this.obtenerMedico(rq.getMedicoId());

        FormulaMedica formula = new FormulaMedica();
        formula.setMascota(mascota);
        formula.setMedico(medico);
        formula.setFechaCreacion(LocalDateTime.now());
        formula.setObservaciones(rq.getObservaciones());

        formulaMedicaRepository.save(formula);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Fórmula médica guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizar(FormulaMedicaRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El ID de la fórmula médica es obligatorio para actualizar");
        }

        Optional<FormulaMedica> opt = formulaMedicaRepository.findById(rq.getId());
        if (opt.isEmpty()) {
            throw new BadRequestException("La fórmula médica con ID " + rq.getId() + " no existe");
        }

        Mascota mascota = this.obtenerMascota(rq.getMascotaId());
        Medico medico = this.obtenerMedico(rq.getMedicoId());

        FormulaMedica formula = opt.get();
        formula.setMascota(mascota);
        formula.setMedico(medico);
        formula.setObservaciones(rq.getObservaciones());

        formulaMedicaRepository.save(formula);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Fórmula médica actualizada correctamente");
        return respuesta;
    }

    private void validar(FormulaMedicaRq rq) throws BadRequestException {
        if (rq == null || rq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota es obligatorio");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
    }

    private Mascota obtenerMascota(Long id) throws BadRequestException {
        Optional<Mascota> opt = mascotaRepository.findById(id);
        if (opt.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + id + " no existe");
        }
        return opt.get();
    }

    private Medico obtenerMedico(Long id) throws BadRequestException {
        Optional<Medico> opt = medicoRepository.findById(id);
        if (opt.isEmpty()) {
            throw new BadRequestException("El médico con ID " + id + " no existe");
        }
        return opt.get();
    }
}
