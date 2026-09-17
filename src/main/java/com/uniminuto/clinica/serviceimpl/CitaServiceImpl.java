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
import com.uniminuto.clinica.util.FechaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementacion de los servicios de negocio de la entidad Cita.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /** Repositorio de acceso a datos de las citas. */
    @Autowired
    private CitaRepository citaRepository;

    /** Repositorio usado para validar la mascota asociada a la cita. */
    @Autowired
    private MascotaRepository mascotaRepository;

    /** Repositorio usado para validar el medico asociado a la cita. */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cita> listarCitasPorRangoFechas(LocalDate fechaInicial,
                                                LocalDate fechaFinal)
            throws BadRequestException {
        FechaUtil.validarRango(fechaInicial, fechaFinal);
        return this.citaRepository.findByFechaCitaBetweenOrderByFechaCitaDesc(
                FechaUtil.inicioDelDia(fechaInicial),
                FechaUtil.finDelDia(fechaFinal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(citaRq);

        // Paso 2. Construir la cita con las relaciones ya validadas.
        Cita cita = new Cita();
        cita.setFechaCita(citaRq.getFechaCita());
        cita.setMotivo(citaRq.getMotivo());
        cita.setEstado(citaRq.getEstado() == null ? "PENDIENTE" : citaRq.getEstado());
        cita.setMascota(this.obtenerMascota(citaRq.getMascotaId()));
        cita.setMedico(this.obtenerMedico(citaRq.getMedicoId()));
        cita.setFechaCreacion(LocalDateTime.now());

        this.citaRepository.save(cita);

        // Paso 3. Retornar la respuesta al cliente.
        return this.construirRespuesta("Cita creada correctamente.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada.
        this.validarObjetoEntrada(citaRq);

        if (citaRq.getCitaId() == null) {
            throw new BadRequestException(
                    "El id de la cita es obligatorio para actualizar.");
        }

        // Paso 2. Validar que la cita exista en la base de datos.
        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException(
                    "La cita con id " + citaRq.getCitaId() + " no existe.");
        }

        // Paso 3. Actualizar los datos de la cita encontrada.
        Cita cita = optCita.get();
        cita.setFechaCita(citaRq.getFechaCita());
        cita.setMotivo(citaRq.getMotivo());
        cita.setEstado(citaRq.getEstado() == null ? cita.getEstado() : citaRq.getEstado());
        cita.setMascota(this.obtenerMascota(citaRq.getMascotaId()));
        cita.setMedico(this.obtenerMedico(citaRq.getMedicoId()));
        cita.setFechaModificacion(LocalDateTime.now());

        this.citaRepository.save(cita);

        return this.construirRespuesta("Cita actualizada correctamente.");
    }

    /**
     * Valida los campos obligatorios del objeto de entrada de una cita.
     *
     * @param citaRq Objeto de entrada a validar.
     * @throws BadRequestException Si algun campo obligatorio no viene informado.
     */
    private void validarObjetoEntrada(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede ser nulo.");
        }
        if (citaRq.getFechaCita() == null) {
            throw new BadRequestException("La fecha de la cita es obligatoria.");
        }
        if (citaRq.getMotivo() == null || citaRq.getMotivo().trim().isEmpty()) {
            throw new BadRequestException("El motivo de la cita es obligatorio.");
        }
        if (citaRq.getMascotaId() == null || citaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio.");
        }
        if (citaRq.getMedicoId() == null || citaRq.getMedicoId() <= 0) {
            throw new BadRequestException("El id del medico es obligatorio.");
        }
    }

    /**
     * Obtiene la mascota que se asocia a la cita validando que exista.
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
     * Obtiene el medico que se asocia a la cita validando que exista.
     *
     * @param medicoId Identificador del medico.
     * @return Medico encontrado.
     * @throws BadRequestException Si el medico no existe.
     */
    private Medico obtenerMedico(Long medicoId) throws BadRequestException {
        return this.medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException(
                        "El medico con id " + medicoId + " no existe."));
    }

    /**
     * Construye la respuesta estandar exitosa de los servicios de cita.
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
