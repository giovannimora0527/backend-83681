package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de historias médicas.
 */
@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    /**
     * Repositorio de historias médicas.
     */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Repositorio de mascotas, usado para validar la mascota de la historia.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Repositorio de médicos, usado para validar el médico de la historia.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS crearHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(historiaMedicaRq);

        // Paso 2. Validar que la mascota exista.
        Mascota mascota = this.obtenerMascota(historiaMedicaRq.getMascotaId());

        // Paso 3. Validar que el médico exista.
        Medico medico = this.obtenerMedico(historiaMedicaRq.getMedicoId());

        // Paso 4. Construir y guardar la nueva historia médica.
        HistoriaMedica historiaMedica = new HistoriaMedica();
        historiaMedica.setDiagnostico(historiaMedicaRq.getDiagnostico());
        historiaMedica.setTratamiento(historiaMedicaRq.getTratamiento());
        historiaMedica.setMascota(mascota);
        historiaMedica.setMedico(medico);
        historiaMedica.setFechaCreacion(LocalDateTime.now());

        this.historiaMedicaRepository.save(historiaMedica);

        // Paso 5. Retornar la respuesta de la operación.
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia médica creada correctamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HistoriaMedica> listarHistoriasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        // Paso 1. Validar que las fechas de filtro sean válidas.
        this.validarRangoFechas(fechaInicial, fechaFinal);

        // Paso 2. Consultar las historias médicas dentro del rango, ordenadas de la más reciente a la más antigua.
        return this.historiaMedicaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicial, fechaFinal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(historiaMedicaRq);

        if (historiaMedicaRq.getHistoriaId() == null) {
            throw new BadRequestException("El ID de la historia médica no puede estar vacío");
        }

        // Paso 2. Validar que la historia médica exista.
        Optional<HistoriaMedica> optHistoria = this.historiaMedicaRepository.findById(historiaMedicaRq.getHistoriaId());
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia médica con ID " + historiaMedicaRq.getHistoriaId() + " no existe en la base de datos");
        }

        // Paso 3. Validar que la mascota exista.
        Mascota mascota = this.obtenerMascota(historiaMedicaRq.getMascotaId());

        // Paso 4. Validar que el médico exista.
        Medico medico = this.obtenerMedico(historiaMedicaRq.getMedicoId());

        // Paso 5. Actualizar y guardar la historia médica.
        HistoriaMedica historiaMedica = optHistoria.get();
        historiaMedica.setDiagnostico(historiaMedicaRq.getDiagnostico());
        historiaMedica.setTratamiento(historiaMedicaRq.getTratamiento());
        historiaMedica.setMascota(mascota);
        historiaMedica.setMedico(medico);
        historiaMedica.setFechaModificacion(LocalDateTime.now());

        this.historiaMedicaRepository.save(historiaMedica);

        // Paso 6. Retornar la respuesta de la operación.
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia médica actualizada correctamente");
        return respuesta;
    }

    /**
     * Valida que el objeto de entrada de una historia médica tenga los
     * datos obligatorios.
     *
     * @param historiaMedicaRq objeto a validar.
     * @throws BadRequestException si algún dato obligatorio falta o es inválido.
     */
    private void validarObjetoEntrada(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        if (historiaMedicaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (historiaMedicaRq.getDiagnostico() == null || historiaMedicaRq.getDiagnostico().trim().isEmpty()) {
            throw new BadRequestException("El diagnóstico no puede estar vacío");
        }
        if (historiaMedicaRq.getTratamiento() == null || historiaMedicaRq.getTratamiento().trim().isEmpty()) {
            throw new BadRequestException("El tratamiento no puede estar vacío");
        }
        if (historiaMedicaRq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota no puede estar vacío");
        }
        if (historiaMedicaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico no puede estar vacío");
        }
    }

    /**
     * Valida que un rango de fechas sea válido (ninguna sea nula y la
     * fecha inicial no sea posterior a la final).
     *
     * @param fechaInicial fecha inicial del rango.
     * @param fechaFinal   fecha final del rango.
     * @throws BadRequestException si el rango de fechas es inválido.
     */
    private void validarRangoFechas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("La fecha inicial y la fecha final son obligatorias");
        }
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    /**
     * Obtiene una mascota por su ID o lanza una excepción si no existe.
     *
     * @param mascotaId ID de la mascota a buscar.
     * @return la mascota encontrada.
     * @throws BadRequestException si la mascota no existe.
     */
    private Mascota obtenerMascota(Long mascotaId) throws BadRequestException {
        Optional<Mascota> optMascota = this.mascotaRepository.findById(mascotaId);
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + mascotaId + " no existe en la base de datos");
        }
        return optMascota.get();
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
