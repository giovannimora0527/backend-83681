package com.uniminuto.clinica.serviceimpl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de citas.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /**
     * Repositorio de citas.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Repositorio de mascotas, usado para validar la mascota de la cita.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Repositorio de médicos, usado para validar el médico de la cita.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cita> filtrarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        // Paso 1. Validar que las fechas de filtro sean válidas.
        this.validarRangoFechas(fechaInicial, fechaFinal);

        // Paso 2. Consultar las citas dentro del rango, ordenadas de la más reciente a la más antigua.
        return this.citaRepository.findByFechaCitaBetweenOrderByFechaCitaDesc(fechaInicial, fechaFinal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS crearCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(citaRq);

        // Paso 2. Validar que la mascota exista.
        Mascota mascota = this.obtenerMascota(citaRq.getMascotaId());

        // Paso 3. Validar que el médico exista.
        Medico medico = this.obtenerMedico(citaRq.getMedicoId());

        // Paso 4. Construir y guardar la nueva cita.
        Cita cita = new Cita();
        cita.setFechaCita(citaRq.getFechaCita());
        cita.setMotivo(citaRq.getMotivo());
        cita.setEstado(citaRq.getEstado());
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaCreacion(LocalDateTime.now());

        this.citaRepository.save(cita);

        // Paso 5. Retornar la respuesta de la operación.
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita creada correctamente");
        return respuesta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(citaRq);

        if (citaRq.getCitaId() == null) {
            throw new BadRequestException("El ID de la cita no puede estar vacío");
        }

        // Paso 2. Validar que la cita exista.
        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + citaRq.getCitaId() + " no existe en la base de datos");
        }

        // Paso 3. Validar que la mascota exista.
        Mascota mascota = this.obtenerMascota(citaRq.getMascotaId());

        // Paso 4. Validar que el médico exista.
        Medico medico = this.obtenerMedico(citaRq.getMedicoId());

        // Paso 5. Actualizar y guardar la cita.
        Cita cita = optCita.get();
        cita.setFechaCita(citaRq.getFechaCita());
        cita.setMotivo(citaRq.getMotivo());
        cita.setEstado(citaRq.getEstado());
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaModificacion(LocalDateTime.now());

        this.citaRepository.save(cita);

        // Paso 6. Retornar la respuesta de la operación.
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita actualizada correctamente");
        return respuesta;
    }

    /**
     * Valida que el objeto de entrada de una cita tenga los datos
     * obligatorios.
     *
     * @param citaRq objeto a validar.
     * @throws BadRequestException si algún dato obligatorio falta o es inválido.
     */
    private void validarObjetoEntrada(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (citaRq.getFechaCita() == null) {
            throw new BadRequestException("La fecha de la cita no puede estar vacía");
        }
        if (citaRq.getMotivo() == null || citaRq.getMotivo().trim().isEmpty()) {
            throw new BadRequestException("El motivo de la cita no puede estar vacío");
        }
        if (citaRq.getEstado() == null || citaRq.getEstado().trim().isEmpty()) {
            throw new BadRequestException("El estado de la cita no puede estar vacío");
        }
        if (citaRq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota no puede estar vacío");
        }
        if (citaRq.getMedicoId() == null) {
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
