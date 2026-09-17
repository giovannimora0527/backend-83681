package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion de los servicios de negocio de la entidad AnotacionHistoria.
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /** Repositorio de acceso a datos de las anotaciones de historia. */
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /** Repositorio usado para validar la historia medica relacionada. */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AnotacionHistoria> listarAnotaciones(Long historiaId)
            throws BadRequestException {
        if (historiaId == null) {
            return this.anotacionHistoriaRepository.findAllByOrderByFechaCreacionDesc();
        }

        // Se valida la historia antes de filtrar para dar un error claro.
        this.obtenerHistoriaMedica(historiaId);
        return this.anotacionHistoriaRepository
                .findByHistoriaMedicaHistoriaIdOrderByFechaCreacionDesc(historiaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(anotacionHistoriaRq);

        // Paso 2. Construir la anotacion con la historia medica validada.
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        anotacion.setHistoriaMedica(
                this.obtenerHistoriaMedica(anotacionHistoriaRq.getHistoriaId()));
        anotacion.setFechaCreacion(LocalDateTime.now());

        this.anotacionHistoriaRepository.save(anotacion);

        return this.construirRespuesta("Anotacion creada correctamente.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(anotacionHistoriaRq);

        if (anotacionHistoriaRq.getAnotacionId() == null) {
            throw new BadRequestException(
                    "El id de la anotacion es obligatorio para actualizar.");
        }

        // Paso 2. Validar que la anotacion exista.
        Optional<AnotacionHistoria> optAnotacion =
                this.anotacionHistoriaRepository.findById(
                        anotacionHistoriaRq.getAnotacionId());
        if (optAnotacion.isEmpty()) {
            throw new BadRequestException("La anotacion con id "
                    + anotacionHistoriaRq.getAnotacionId() + " no existe.");
        }

        // Paso 3. Actualizar los datos de la anotacion encontrada.
        AnotacionHistoria anotacion = optAnotacion.get();
        anotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        anotacion.setHistoriaMedica(
                this.obtenerHistoriaMedica(anotacionHistoriaRq.getHistoriaId()));
        anotacion.setFechaModificacion(LocalDateTime.now());

        this.anotacionHistoriaRepository.save(anotacion);

        return this.construirRespuesta("Anotacion actualizada correctamente.");
    }

    /**
     * Valida los campos obligatorios de la anotacion recibida.
     *
     * @param anotacionHistoriaRq Objeto de entrada a validar.
     * @throws BadRequestException Si algun campo obligatorio no viene informado.
     */
    private void validarObjetoEntrada(AnotacionHistoriaRq anotacionHistoriaRq)
            throws BadRequestException {
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede ser nulo.");
        }
        if (anotacionHistoriaRq.getDescripcion() == null
                || anotacionHistoriaRq.getDescripcion().trim().isEmpty()) {
            throw new BadRequestException("La descripcion de la anotacion es obligatoria.");
        }
        if (anotacionHistoriaRq.getHistoriaId() == null
                || anotacionHistoriaRq.getHistoriaId() <= 0) {
            throw new BadRequestException("El id de la historia medica es obligatorio.");
        }
    }

    /**
     * Obtiene la historia medica relacionada validando que exista.
     *
     * @param historiaId Identificador de la historia medica.
     * @return Historia medica encontrada.
     * @throws BadRequestException Si la historia medica no existe.
     */
    private HistoriaMedica obtenerHistoriaMedica(Long historiaId)
            throws BadRequestException {
        return this.historiaMedicaRepository.findById(historiaId)
                .orElseThrow(() -> new BadRequestException(
                        "La historia medica con id " + historiaId + " no existe."));
    }

    /**
     * Construye la respuesta estandar exitosa de los servicios de anotacion.
     *
     * @param mensaje Mensaje a devolver al cliente.
     * @return Respuesta con codigo 200 y el mensaje recibido.
     */
    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
