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
    public List<Cita> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }
        return citaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicio, fechaFin);
    }

    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(citaRq);

        // Paso 2. Validar que el cliente, la mascota y el médico existan
        Cliente cliente = this.buscarCliente(citaRq.getClienteId());
        Mascota mascota = this.buscarMascota(citaRq.getMascotaId());
        Medico medico = this.buscarMedico(citaRq.getMedicoId());

        // Paso 3. Crear la entidad cita a guardar
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(citaRq.getEstado());
        cita.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(cita);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(citaRq);

        if (citaRq.getId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio para actualizar");
        }

        // Paso 2. Validar que la cita, el cliente, la mascota y el médico existan
        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + citaRq.getId() + " no existe en la base de datos");
        }

        Cliente cliente = this.buscarCliente(citaRq.getClienteId());
        Mascota mascota = this.buscarMascota(citaRq.getMascotaId());
        Medico medico = this.buscarMedico(citaRq.getMedicoId());

        // Paso 3. Actualizar los datos de la cita
        Cita cita = optCita.get();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(citaRq.getEstado());
        cita.setMotivo(citaRq.getMotivo());

        this.citaRepository.save(cita);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita actualizada correctamente");
        return respuesta;
    }

    /**
     * Valida los campos obligatorios del objeto de entrada de una cita.
     *
     * @param citaRq objeto a validar.
     * @throws BadRequestException si algún campo obligatorio falta.
     */
    private void validarObjetoEntrada(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (citaRq.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente es obligatorio");
        }
        if (citaRq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota es obligatorio");
        }
        if (citaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita son obligatorias");
        }
        if (citaRq.getEstado() == null || citaRq.getEstado().trim().isEmpty()) {
            throw new BadRequestException("El estado de la cita es obligatorio");
        }
    }

    /**
     * Busca un cliente por id o lanza BadRequestException si no existe.
     */
    private Cliente buscarCliente(Long clienteId) throws BadRequestException {
        Optional<Cliente> optCliente = this.clienteRepository.findById(clienteId);
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + clienteId + " no existe en la base de datos");
        }
        return optCliente.get();
    }

    /**
     * Busca una mascota por id o lanza BadRequestException si no existe.
     */
    private Mascota buscarMascota(Long mascotaId) throws BadRequestException {
        Optional<Mascota> optMascota = this.mascotaRepository.findById(mascotaId);
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + mascotaId + " no existe en la base de datos");
        }
        return optMascota.get();
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