package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import com.uniminuto.clinica.util.FechaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion de los servicios de negocio de la entidad HistoriaMedica.
 */
@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    /** Repositorio de acceso a datos de las historias medicas. */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /** Repositorio usado para validar la mascota de la historia medica. */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HistoriaMedica> listarHistoriasMedicas(LocalDate fechaInicial,
                                                       LocalDate fechaFinal)
            throws BadRequestException {
        // Si no se envia ninguna fecha se devuelve el listado completo.
        if (fechaInicial == null && fechaFinal == null) {
            return this.historiaMedicaRepository.findAllByOrderByFechaCreacionDesc();
        }

        FechaUtil.validarRango(fechaInicial, fechaFinal);
        return this.historiaMedicaRepository
                .findByFechaCreacionBetweenOrderByFechaCreacionDesc(
                        FechaUtil.inicioDelDia(fechaInicial),
                        FechaUtil.finDelDia(fechaFinal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(historiaMedicaRq);

        // Paso 2. Construir la historia medica con la mascota validada.
        HistoriaMedica historiaMedica = new HistoriaMedica();
        historiaMedica.setDiagnostico(historiaMedicaRq.getDiagnostico());
        historiaMedica.setTratamiento(historiaMedicaRq.getTratamiento());
        historiaMedica.setMascota(this.obtenerMascota(historiaMedicaRq.getMascotaId()));
        historiaMedica.setFechaCreacion(LocalDateTime.now());

        this.historiaMedicaRepository.save(historiaMedica);

        return this.construirRespuesta("Historia medica creada correctamente.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(historiaMedicaRq);

        if (historiaMedicaRq.getHistoriaId() == null) {
            throw new BadRequestException(
                    "El id de la historia medica es obligatorio para actualizar.");
        }

        // Paso 2. Validar que la historia medica exista.
        Optional<HistoriaMedica> optHistoria =
                this.historiaMedicaRepository.findById(historiaMedicaRq.getHistoriaId());
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia medica con id "
                    + historiaMedicaRq.getHistoriaId() + " no existe.");
        }

        // Paso 3. Actualizar los datos de la historia encontrada.
        HistoriaMedica historiaMedica = optHistoria.get();
        historiaMedica.setDiagnostico(historiaMedicaRq.getDiagnostico());
        historiaMedica.setTratamiento(historiaMedicaRq.getTratamiento());
        historiaMedica.setMascota(this.obtenerMascota(historiaMedicaRq.getMascotaId()));
        historiaMedica.setFechaModificacion(LocalDateTime.now());

        this.historiaMedicaRepository.save(historiaMedica);

        return this.construirRespuesta("Historia medica actualizada correctamente.");
    }

    /**
     * Valida los campos obligatorios de la historia medica recibida.
     *
     * @param historiaMedicaRq Objeto de entrada a validar.
     * @throws BadRequestException Si algun campo obligatorio no viene informado.
     */
    private void validarObjetoEntrada(HistoriaMedicaRq historiaMedicaRq)
            throws BadRequestException {
        if (historiaMedicaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede ser nulo.");
        }
        if (historiaMedicaRq.getDiagnostico() == null
                || historiaMedicaRq.getDiagnostico().trim().isEmpty()) {
            throw new BadRequestException("El diagnostico es obligatorio.");
        }
        if (historiaMedicaRq.getMascotaId() == null
                || historiaMedicaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio.");
        }
    }

    /**
     * Obtiene la mascota de la historia medica validando que exista.
     *
     * @param mascotaId Identificador de la mascota.
     * @return Mascota encontrada.
     * @throws BadRequestException Si la mascota no existe.
     */
    private Mascota obtenerMascota(Long mascotaId) throws BadRequestException {
        return this.mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new BadRequestException(
                        "La mascota con id " + mascotaId + " no existe."));
    }

    /**
     * Construye la respuesta estandar exitosa de los servicios de historia medica.
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
