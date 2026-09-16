package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public List<Cita> filtrarCitasPorFecha(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        // Paso 1. Validar que ambas fechas hayan sido enviadas.
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("La fecha inicial y la fecha final son obligatorias");
        }

        // Paso 2. Validar la coherencia del rango de fechas.
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }

        // Paso 3. Consultar las citas del rango, ya ordenadas descendentemente.
        return this.citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial, fechaFinal);
    }

    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Validar los campos obligatorios del objeto de entrada.
        this.validarObjetoEntrada(citaRq, true);

        // Paso 2. Construir la cita con las entidades relacionadas ya validadas.
        Cita cita = new Cita();
        cita.setCliente(this.obtenerCliente(citaRq.getClienteId()));
        cita.setMascota(this.obtenerMascota(citaRq.getMascotaId()));
        cita.setMedico(this.obtenerMedico(citaRq.getMedicoId()));
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(citaRq.getEstado());
        cita.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(cita);

        // Paso 3. Retornar la respuesta de la operación.
        return this.construirRespuesta("Cita guardada correctamente");
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Validar los campos obligatorios, incluyendo el identificador.
        this.validarObjetoEntrada(citaRq, false);

        // Paso 2. Validar que la cita a actualizar exista.
        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + citaRq.getId() + " no existe en la base de datos");
        }

        // Paso 3. Actualizar los datos con las entidades relacionadas ya validadas.
        Cita cita = optCita.get();
        cita.setCliente(this.obtenerCliente(citaRq.getClienteId()));
        cita.setMascota(this.obtenerMascota(citaRq.getMascotaId()));
        cita.setMedico(this.obtenerMedico(citaRq.getMedicoId()));
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(citaRq.getEstado());
        cita.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(cita);

        // Paso 4. Retornar la respuesta de la operación.
        return this.construirRespuesta("Cita actualizada correctamente");
    }

    /**
     * Busca un cliente por su identificador y valida que exista.
     */
    private Cliente obtenerCliente(Long clienteId) throws BadRequestException {
        return this.clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BadRequestException("El cliente con ID " + clienteId + " no existe en la base de datos"));
    }

    /**
     * Busca una mascota por su identificador y valida que exista.
     */
    private Mascota obtenerMascota(Long mascotaId) throws BadRequestException {
        return this.mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new BadRequestException("La mascota con ID " + mascotaId + " no existe en la base de datos"));
    }

    /**
     * Busca un médico por su identificador y valida que exista.
     */
    private Medico obtenerMedico(Long medicoId) throws BadRequestException {
        return this.medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El médico con ID " + medicoId + " no existe en la base de datos"));
    }

    /**
     * Construye una respuesta exitosa estándar para las operaciones del servicio.
     */
    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    /**
     * Valida los campos obligatorios de una solicitud de cita..
     */
    private void validarObjetoEntrada(CitaRq citaRq, boolean esCreacion) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        if (!esCreacion && citaRq.getId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio para actualizar");
        }

        if (citaRq.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente no puede estar vacío");
        }

        if (citaRq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota no puede estar vacío");
        }

        if (citaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico no puede estar vacío");
        }

        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita no puede estar vacía");
        }

        if (citaRq.getEstado() == null || citaRq.getEstado().trim().isEmpty()) {
            throw new BadRequestException("El estado de la cita no puede estar vacío");
        }
    }
}