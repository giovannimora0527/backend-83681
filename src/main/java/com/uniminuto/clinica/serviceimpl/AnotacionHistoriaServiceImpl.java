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

/**
 * Implementación del servicio de anotaciones de historia médica.
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /**
     * Repositorio de anotaciones de historia médica.
     */
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * Repositorio de historias médicas, usado para validar la historia
     * a la que pertenece la anotación.
     */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Repositorio de médicos, usado para validar el médico de la
     * anotación.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS crearAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(anotacionHistoriaRq);

        // Paso 2. Validar que la historia médica exista.
        HistoriaMedica historiaMedica = this.obtenerHistoriaMedica(anotacionHistoriaRq.getHistoriaId());

        // Paso 3. Validar que el médico exista.
        Medico medico = this.obtenerMedico(anotacionHistoriaRq.getMedicoId());

        // Paso 4. Construir y guardar la nueva anotación.
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setObservacion(anotacionHistoriaRq.getObservacion());
        anotacion.setHistoriaMedica(historiaMedica);
        anotacion.setMedico(medico);
        anotacion.setFechaCreacion(LocalDateTime.now());

        this.anotacionHistoriaRepository.save(anotacion);

        // Paso 5. Retornar la respuesta de la operación.
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotación de historia creada correctamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AnotacionHistoria> listarAnotacionesPorHistoria(Long historiaId) throws BadRequestException {
        // Paso 1. Validar que el identificador y la historia médica sean válidos.
        if (historiaId == null) {
            throw new BadRequestException("El ID de la historia médica no puede estar vacío");
        }
        this.obtenerHistoriaMedica(historiaId);

        // Paso 2. Consultar las anotaciones de la historia médica, ordenadas de la más reciente a la más antigua.
        return this.anotacionHistoriaRepository.findByHistoriaMedica_HistoriaIdOrderByFechaCreacionDesc(historiaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(anotacionHistoriaRq);

        if (anotacionHistoriaRq.getAnotacionId() == null) {
            throw new BadRequestException("El ID de la anotación no puede estar vacío");
        }

        // Paso 2. Validar que la anotación exista.
        Optional<AnotacionHistoria> optAnotacion = this.anotacionHistoriaRepository.findById(anotacionHistoriaRq.getAnotacionId());
        if (optAnotacion.isEmpty()) {
            throw new BadRequestException("La anotación con ID " + anotacionHistoriaRq.getAnotacionId() + " no existe en la base de datos");
        }

        // Paso 3. Validar que la historia médica exista.
        HistoriaMedica historiaMedica = this.obtenerHistoriaMedica(anotacionHistoriaRq.getHistoriaId());

        // Paso 4. Validar que el médico exista.
        Medico medico = this.obtenerMedico(anotacionHistoriaRq.getMedicoId());

        // Paso 5. Actualizar y guardar la anotación.
        AnotacionHistoria anotacion = optAnotacion.get();
        anotacion.setObservacion(anotacionHistoriaRq.getObservacion());
        anotacion.setHistoriaMedica(historiaMedica);
        anotacion.setMedico(medico);
        anotacion.setFechaModificacion(LocalDateTime.now());

        this.anotacionHistoriaRepository.save(anotacion);

        // Paso 6. Retornar la respuesta de la operación.
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotación de historia actualizada correctamente");
        return respuesta;
    }

    /**
     * Valida que el objeto de entrada de una anotación tenga los datos
     * obligatorios.
     *
     * @param anotacionHistoriaRq objeto a validar.
     * @throws BadRequestException si algún dato obligatorio falta o es inválido.
     */
    private void validarObjetoEntrada(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (anotacionHistoriaRq.getObservacion() == null || anotacionHistoriaRq.getObservacion().trim().isEmpty()) {
            throw new BadRequestException("La observación no puede estar vacía");
        }
        if (anotacionHistoriaRq.getHistoriaId() == null) {
            throw new BadRequestException("El ID de la historia médica no puede estar vacío");
        }
        if (anotacionHistoriaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico no puede estar vacío");
        }
    }

    /**
     * Obtiene una historia médica por su ID o lanza una excepción si no
     * existe.
     *
     * @param historiaId ID de la historia médica a buscar.
     * @return la historia médica encontrada.
     * @throws BadRequestException si la historia médica no existe.
     */
    private HistoriaMedica obtenerHistoriaMedica(Long historiaId) throws BadRequestException {
        Optional<HistoriaMedica> optHistoria = this.historiaMedicaRepository.findById(historiaId);
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia médica con ID " + historiaId + " no existe en la base de datos");
        }
        return optHistoria.get();
    }

    /**
     * Obtiene un médico por su ID o lanza una excepción si no existe.
     *
     * @param medicoId ID del médico a buscar.
     * @return el médico encontrado.
     * @throws BadRequestException si el médico no existe.
     */
    private Medico obtenerMedico(Long medicoId) throws BadRequestException {
        Optional<Medico> optMedico = this.medicoRepository.findById(medicoId);
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + medicoId + " no existe en la base de datos");
        }
        return optMedico.get();
    }
}
