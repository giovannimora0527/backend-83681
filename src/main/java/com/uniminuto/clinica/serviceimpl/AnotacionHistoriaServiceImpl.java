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
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public List<AnotacionHistoria> listarAnotaciones(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }
        return anotacionHistoriaRepository.findAllByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }

    @Override
    public MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(anotacionHistoriaRq);

        // Paso 2. Validar que la historia médica y el médico existan
        HistoriaMedica historia = this.buscarHistoria(anotacionHistoriaRq.getHistoriaId());
        Medico medico = this.buscarMedico(anotacionHistoriaRq.getMedicoId());

        // Paso 3. Crear la anotación
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoria(historia);
        anotacion.setMedico(medico);
        anotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        anotacion.setFecha(LocalDateTime.now());

        this.anotacionHistoriaRepository.save(anotacion);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotación de historia guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(anotacionHistoriaRq);

        if (anotacionHistoriaRq.getId() == null) {
            throw new BadRequestException("El ID de la anotación es obligatorio para actualizar");
        }

        // Paso 2. Validar que la anotación, la historia médica y el médico existan
        Optional<AnotacionHistoria> optAnotacion = this.anotacionHistoriaRepository.findById(anotacionHistoriaRq.getId());
        if (optAnotacion.isEmpty()) {
            throw new BadRequestException("La anotación con ID " + anotacionHistoriaRq.getId() + " no existe en la base de datos");
        }

        HistoriaMedica historia = this.buscarHistoria(anotacionHistoriaRq.getHistoriaId());
        Medico medico = this.buscarMedico(anotacionHistoriaRq.getMedicoId());

        // Paso 3. Actualizar los datos de la anotación (la fecha original no se modifica)
        AnotacionHistoria anotacion = optAnotacion.get();
        anotacion.setHistoria(historia);
        anotacion.setMedico(medico);
        anotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());

        this.anotacionHistoriaRepository.save(anotacion);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotación de historia actualizada correctamente");
        return respuesta;
    }

    /**
     * Valida los campos obligatorios del objeto de entrada de una anotación.
     */
    private void validarObjetoEntrada(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (anotacionHistoriaRq.getHistoriaId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio");
        }
        if (anotacionHistoriaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (anotacionHistoriaRq.getDescripcion() == null || anotacionHistoriaRq.getDescripcion().trim().isEmpty()) {
            throw new BadRequestException("La descripción de la anotación es obligatoria");
        }
    }

    /**
     * Busca una historia médica por id o lanza BadRequestException si no existe.
     */
    private HistoriaMedica buscarHistoria(Long historiaId) throws BadRequestException {
        Optional<HistoriaMedica> optHistoria = this.historiaMedicaRepository.findById(historiaId);
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia médica con ID " + historiaId + " no existe en la base de datos");
        }
        return optHistoria.get();
    }

    /**
     * Busca un médico por id o lanza BadRequestException si no existe.
     */
    private Medico buscarMedico(Long medicoId) throws BadRequestException {
        Optional<Medico> optMedico = this.medicoRepository.findById(medicoId);
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + medicoId + " no existe en la base de datos");
        }
        return optMedico.get();
    }
}