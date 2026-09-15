package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.UsuarioRS;
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
    private MedicoRepository medicoRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public List<Cita> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        // Si no vienen fechas, retornar todas ordenadas de la más reciente a la más antigua
        if (fechaInicial == null && fechaFinal == null) {
            return this.citaRepository.findAllByOrderByFechaHoraDesc();
        }

        // Para filtrar se requieren ambas fechas
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("Para filtrar es necesario suministrar fechaInicial y fechaFinal");
        }

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }

        return this.citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial, fechaFinal);
    }

    @Override
    public UsuarioRS guardarCita(CitaRq citaRq) throws BadRequestException {
        validarObjetoEntrada(citaRq);

        this.validarCliente(citaRq.getClienteId());
        this.validarMedico(citaRq.getMedicoId());
        this.validarMascota(citaRq.getMascotaId());

        Cita cita = new Cita();
        cita.setClienteId(citaRq.getClienteId());
        cita.setMedicoId(citaRq.getMedicoId());
        cita.setMascotaId(citaRq.getMascotaId());
        cita.setFechaHora(citaRq.getFechaHora() != null ? citaRq.getFechaHora() : LocalDateTime.now());
        cita.setMotivo(citaRq.getMotivo().trim());
        cita.setEstado(citaRq.getEstado().trim());

        this.citaRepository.save(cita);

        UsuarioRS respuesta = new UsuarioRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita guardada correctamente");
        return respuesta;
    }

    @Override
    public UsuarioRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        validarObjetoEntrada(citaRq);

        if (citaRq.getId() == null || citaRq.getId() <= 0) {
            throw new BadRequestException("El ID de la cita no puede estar vacío ni ser menor o igual a cero");
        }

        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + citaRq.getId() + " no existe en la base de datos");
        }

        this.validarCliente(citaRq.getClienteId());
        this.validarMedico(citaRq.getMedicoId());
        this.validarMascota(citaRq.getMascotaId());

        Cita cita = optCita.get();
        cita.setClienteId(citaRq.getClienteId());
        cita.setMedicoId(citaRq.getMedicoId());
        cita.setMascotaId(citaRq.getMascotaId());
        cita.setFechaHora(citaRq.getFechaHora() != null ? citaRq.getFechaHora() : cita.getFechaHora() != null ? cita.getFechaHora() : LocalDateTime.now());
        cita.setMotivo(citaRq.getMotivo().trim());
        cita.setEstado(citaRq.getEstado().trim());

        this.citaRepository.save(cita);

        UsuarioRS respuesta = new UsuarioRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Cita actualizada correctamente");
        return respuesta;
    }

    private void validarObjetoEntrada(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        if (citaRq.getClienteId() == null || citaRq.getClienteId() <= 0) {
            throw new BadRequestException("El ID del cliente no puede estar vacío ni ser menor o igual a cero");
        }

        if (citaRq.getMedicoId() == null || citaRq.getMedicoId() <= 0) {
            throw new BadRequestException("El ID del médico no puede estar vacío ni ser menor o igual a cero");
        }

        if (citaRq.getMascotaId() == null || citaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El ID de la mascota no puede estar vacío ni ser menor o igual a cero");
        }

        if (citaRq.getMotivo() == null || citaRq.getMotivo().trim().isEmpty()) {
            throw new BadRequestException("El motivo de la cita no puede estar vacío");
        }

        if (citaRq.getEstado() == null || citaRq.getEstado().trim().isEmpty()) {
            throw new BadRequestException("El estado de la cita no puede estar vacío");
        }
    }

    private void validarCliente(Long clienteId) {
        Optional<Cliente> optCliente = this.clienteRepository.findById(clienteId);
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + clienteId + " no existe en la base de datos");
        }
    }

    private void validarMedico(Long medicoId) {
        Optional<Medico> optMedico = this.medicoRepository.findById(medicoId);
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + medicoId + " no existe en la base de datos");
        }
    }

    private void validarMascota(Long mascotaId) {
        Optional<Mascota> optMascota = this.mascotaRepository.findById(mascotaId);
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + mascotaId + " no existe en la base de datos");
        }
    }
}
