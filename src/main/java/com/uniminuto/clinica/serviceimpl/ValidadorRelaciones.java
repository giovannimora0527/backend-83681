package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.util.ValidacionUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Componente reutilizable que obtiene las entidades relacionadas de una
 * operación y valida que existan y que sean coherentes entre sí
 * (por ejemplo, que la mascota pertenezca al cliente). Lo usan los servicios
 * de citas, historias clínicas, anotaciones y fórmulas.
 */
@Component
public class ValidadorRelaciones {

    /**
     * Repositorio de clientes.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Repositorio de mascotas.
     */
    private final MascotaRepository mascotaRepository;

    /**
     * Repositorio de veterinarios.
     */
    private final MedicoRepository medicoRepository;

    /**
     * Repositorio de citas.
     */
    private final CitaRepository citaRepository;

    /**
     * Repositorio de historias clínicas.
     */
    private final HistoriaMedicaRepository historiaMedicaRepository;

    public ValidadorRelaciones(ClienteRepository clienteRepository, MascotaRepository mascotaRepository,
            MedicoRepository medicoRepository, CitaRepository citaRepository,
            HistoriaMedicaRepository historiaMedicaRepository) {
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.medicoRepository = medicoRepository;
        this.citaRepository = citaRepository;
        this.historiaMedicaRepository = historiaMedicaRepository;
    }

    /**
     * Obtiene un cliente activo por su id.
     *
     * @param clienteId identificador del cliente.
     * @return cliente encontrado.
     * @throws BadRequestException si el id es nulo o el cliente está inactivo.
     * @throws NotFoundException   si el cliente no existe.
     */
    public Cliente obtenerCliente(Long clienteId) {
        ValidacionUtil.obligatorio(clienteId, "cliente");
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("El cliente seleccionado no existe."));
        if (Boolean.FALSE.equals(cliente.getActivo())) {
            throw new BadRequestException("El cliente " + cliente.getNombres() + " " + cliente.getApellidos()
                    + " está inactivo.");
        }
        return cliente;
    }

    /**
     * Obtiene una mascota por su id.
     *
     * @param mascotaId identificador de la mascota.
     * @return mascota encontrada.
     * @throws BadRequestException si el id es nulo.
     * @throws NotFoundException   si la mascota no existe.
     */
    public Mascota obtenerMascota(Long mascotaId) {
        ValidacionUtil.obligatorio(mascotaId, "mascota");
        return mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new NotFoundException("La mascota seleccionada no existe."));
    }

    /**
     * Obtiene una mascota y valida que pertenezca a un cliente registrado y
     * que ese cliente sea el indicado.
     *
     * @param mascotaId identificador de la mascota.
     * @param clienteId identificador del cliente que dice ser el dueño.
     * @return mascota encontrada.
     * @throws NotFoundException   si el cliente o la mascota no existen.
     * @throws BadRequestException si la mascota no pertenece al cliente.
     */
    public Mascota obtenerMascotaDeCliente(Long mascotaId, Long clienteId) {
        Cliente cliente = obtenerCliente(clienteId);
        Mascota mascota = obtenerMascota(mascotaId);
        if (mascota.getCliente() == null) {
            throw new BadRequestException("La mascota " + mascota.getNombreMascota()
                    + " no está asociada a un cliente registrado.");
        }
        if (!Objects.equals(mascota.getCliente().getId(), cliente.getId())) {
            throw new BadRequestException("La mascota " + mascota.getNombreMascota()
                    + " no pertenece al cliente seleccionado.");
        }
        return mascota;
    }

    /**
     * Obtiene un veterinario por su id.
     *
     * @param medicoId identificador del veterinario.
     * @return veterinario encontrado.
     * @throws BadRequestException si el id es nulo.
     * @throws NotFoundException   si el veterinario no existe.
     */
    public Medico obtenerMedico(Long medicoId) {
        ValidacionUtil.obligatorio(medicoId, "veterinario");
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new NotFoundException("El veterinario seleccionado no existe."));
    }

    /**
     * Obtiene una cita por su id.
     *
     * @param citaId identificador de la cita.
     * @return cita encontrada.
     * @throws BadRequestException si el id es nulo.
     * @throws NotFoundException   si la cita no existe.
     */
    public Cita obtenerCita(Long citaId) {
        ValidacionUtil.obligatorio(citaId, "cita");
        return citaRepository.findById(citaId)
                .orElseThrow(() -> new NotFoundException("La cita con id " + citaId + " no existe."));
    }

    /**
     * Obtiene una historia clínica por su id.
     *
     * @param historiaId identificador de la historia.
     * @return historia encontrada.
     * @throws BadRequestException si el id es nulo.
     * @throws NotFoundException   si la historia no existe.
     */
    public HistoriaMedica obtenerHistoria(Long historiaId) {
        ValidacionUtil.obligatorio(historiaId, "historia clínica");
        return historiaMedicaRepository.findById(historiaId)
                .orElseThrow(() -> new NotFoundException("La historia clínica con id " + historiaId + " no existe."));
    }
}
