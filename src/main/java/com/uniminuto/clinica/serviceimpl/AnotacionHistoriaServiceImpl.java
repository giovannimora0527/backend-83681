package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    private void validarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {

        // Datos obligatorios
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("Los datos del formulario son obligatorios");
        }

        // ID de la historia médica obligatorio
        if (anotacionHistoriaRq.getHistoriaId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio");
        }

        // ID del médico obligatorio
        if (anotacionHistoriaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }

        // Descripción obligatoria
        if (anotacionHistoriaRq.getDescripcion() == null || anotacionHistoriaRq.getDescripcion().trim().isEmpty()) {
            throw new BadRequestException("La descripción es obligatoria");
        }

        // Diagnóstico o evolución obligatoria
        if (anotacionHistoriaRq.getDiagnosticoEvolucion() == null || anotacionHistoriaRq.getDiagnosticoEvolucion().trim().isEmpty()) {
            throw new BadRequestException("El diagnóstico o evolución es obligatorio");
        }
    }

    @Override
    public List<AnotacionHistoria> listarAnotacionesPorHistoria(Long historiaId) throws BadRequestException {
        if (historiaId == null) {
            throw new BadRequestException("El ID de la historia no puede ser nulo.");
        }
        // Ordenar por fecha descendente
        return anotacionHistoriaRepository.findByHistoriaIdOrderByFechaHoraDesc(historiaId);
    }

    @Override
    public MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {

        // Validaciones obligatorias
        this.validarAnotacionHistoria(anotacionHistoriaRq);

        // Validar que la historia clínica exista
        Optional<HistoriaMedica> historiaOpt = this.historiaMedicaRepository.findById(anotacionHistoriaRq.getHistoriaId());
        if (historiaOpt.isEmpty()) {
            throw new BadRequestException("La historia clínica con ID " + anotacionHistoriaRq.getHistoriaId() + " no existe.");
        }

        // Validar que el médico exista
        Optional<Medico> medicoOptional = this.medicoRepository.findById(anotacionHistoriaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico con ID " + anotacionHistoriaRq.getMedicoId() + " no existe.");
        }

        // Crear y guardar la anotación de la historia clínica
        AnotacionHistoria nuevaAnotacion = new AnotacionHistoria();
        nuevaAnotacion.setHistoria(historiaOpt.get());
        nuevaAnotacion.setMedico(medicoOptional.get());
        nuevaAnotacion.setFechaHora(LocalDateTime.now());
        nuevaAnotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        nuevaAnotacion.setObservacion(anotacionHistoriaRq.getObservacion());
        nuevaAnotacion.setDiagnosticoEvolucion(anotacionHistoriaRq.getDiagnosticoEvolucion());

        this.anotacionHistoriaRepository.save(nuevaAnotacion);

        // Respuesta
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Anotación creada exitosamente");
        return rta;
    }
}