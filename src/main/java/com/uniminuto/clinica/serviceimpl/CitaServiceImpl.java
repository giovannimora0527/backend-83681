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

    /**
     * Repositorio que administra las operaciones de persistencia para citas.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Repositorio utilizado para validar la existencia de clientes relacionados.
     */
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Repositorio utilizado para validar la existencia de médicos relacionados.
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * Repositorio utilizado para validar la existencia de mascotas relacionadas.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Retorna citas filtradas por rango de fechas. Si no recibe fechas, devuelve todas.
     *
     * @param fechaInicial Fecha inicial del rango a consultar.
     * @param fechaFinal Fecha final del rango a consultar.
     * @return Lista de citas ordenadas por fecha más reciente.
     * @throws BadRequestException Si solo se recibe una de las dos fechas o si el rango es inválido.
     */
    @Override
    public List<Cita> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        // Si no vienen fechas, retornar todas ordenadas de la más reciente a la más antigua.
        if (fechaInicial == null && fechaFinal == null) {
            return this.citaRepository.findAllByOrderByFechaHoraDesc();
        }

        // Para filtrar se requieren ambas fechas.
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("Para filtrar es necesario suministrar fechaInicial y fechaFinal");
        }

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }

        return this.citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial, fechaFinal);
    }

    /**
     * Guarda una nueva cita validando los datos del payload y la existencia de
     * cliente, médico y mascota relacionados.
     *
     * @param citaRq Datos de la cita a registrar.
     * @return Respuesta con el estado y el mensaje de la operación.
     * @throws BadRequestException Si la solicitud no cumple validaciones o entidades relacionadas no existen.
     */
    @Override
    public UsuarioRS guardarCita(CitaRq citaRq) throws BadRequestException {
        try {
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
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Error al guardar la cita: " + ex.getMessage());
        }
    }

    /**
     * Actualiza una cita existente con la información recibida.
     *
     * @param citaRq Datos actualizados de la cita.
     * @return Respuesta con el estado y el mensaje de la operación.
     * @throws BadRequestException Si la cita no existe o la entrada es inválida.
     */
    @Override
    public UsuarioRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        try {
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
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Error al actualizar la cita: " + ex.getMessage());
        }
    }

    /**
     * Valida que el payload de entrada tenga todos los campos obligatorios y con valores válidos.
     *
     * @param citaRq Objeto con la información de la cita que llega desde el cliente.
     * @throws BadRequestException Si el payload es nulo o le faltan datos obligatorios.
     */
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

    /**
     * Valida que el cliente indicado exista en la base de datos.
     *
     * @param clienteId Identificador del cliente a validar.
     * @throws BadRequestException Si el cliente no existe.
     */
    private void validarCliente(Long clienteId) {
        Optional<Cliente> optCliente = this.clienteRepository.findById(clienteId);
        if (optCliente.isEmpty()) {
            throw new BadRequestException("El cliente con ID " + clienteId + " no existe en la base de datos");
        }
    }

    /**
     * Valida que el médico indicado exista en la base de datos.
     *
     * @param medicoId Identificador del médico a validar.
     * @throws BadRequestException Si el médico no existe.
     */
    private void validarMedico(Long medicoId) {
        Optional<Medico> optMedico = this.medicoRepository.findById(medicoId);
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + medicoId + " no existe en la base de datos");
        }
    }

    /**
     * Valida que la mascota indicada exista en la base de datos.
     *
     * @param mascotaId Identificador de la mascota a validar.
     * @throws BadRequestException Si la mascota no existe.
     */
    private void validarMascota(Long mascotaId) {
        Optional<Mascota> optMascota = this.mascotaRepository.findById(mascotaId);
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + mascotaId + " no existe en la base de datos");
        }
    }
}
