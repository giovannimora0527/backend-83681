package com.uniminuto.clinica.util;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validaciones que se repiten en varios servicios (citas, historias y formulas):
 * campos obligatorios y existencia de cliente, mascota, veterinario y cita.
 * Al tenerlas en un solo lugar, si una regla cambia se corrige una sola vez.
 */
@Component
public class ValidadorEntidades {

    /**
     * Repositorio de clientes.
     */
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Repositorio de mascotas.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    /**
     * Repositorio de medicos (veterinarios).
     */
    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * Repositorio de citas.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Verifica que un id obligatorio venga informado y sea positivo.
     *
     * @param id valor a validar.
     * @param nombreCampo nombre del campo para el mensaje de error.
     * @throws BadRequestException si el id es nulo o menor o igual a cero.
     */
    public void validarIdObligatorio(Long id, String nombreCampo) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El campo " + nombreCampo + " es obligatorio.");
        }
    }

    /**
     * Verifica que un texto obligatorio no venga nulo ni vacio.
     *
     * @param valor texto a validar.
     * @param nombreCampo nombre del campo para el mensaje de error.
     * @throws BadRequestException si el texto es nulo o vacio.
     */
    public void validarTextoObligatorio(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new BadRequestException("El campo " + nombreCampo + " es obligatorio.");
        }
    }

    /**
     * Busca un cliente por id.
     *
     * @param clienteId id del cliente.
     * @return cliente encontrado.
     * @throws NotFoundException si el cliente no existe.
     */
    public Cliente obtenerCliente(Long clienteId) {
        validarIdObligatorio(clienteId, "cliente");
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("El cliente con ID " + clienteId + " no existe."));
    }

    /**
     * Busca una mascota por id y verifica que pertenezca a un cliente registrado.
     *
     * @param mascotaId id de la mascota.
     * @return mascota encontrada.
     * @throws NotFoundException si la mascota no existe.
     * @throws BadRequestException si la mascota no tiene cliente asociado.
     */
    public Mascota obtenerMascota(Long mascotaId) {
        validarIdObligatorio(mascotaId, "mascota");
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new NotFoundException("La mascota con ID " + mascotaId + " no existe."));
        if (mascota.getCliente() == null) {
            throw new BadRequestException("La mascota " + mascota.getNombreMascota()
                    + " no está asociada a ningún cliente registrado.");
        }
        return mascota;
    }

    /**
     * Valida que el cliente exista, que la mascota exista y que la mascota
     * pertenezca a ese cliente. Es la regla "Cliente -> Mascotas del cliente".
     *
     * @param clienteId id del cliente seleccionado.
     * @param mascotaId id de la mascota seleccionada.
     * @return mascota validada.
     * @throws BadRequestException si la mascota no pertenece al cliente.
     */
    public Mascota obtenerMascotaDelCliente(Long clienteId, Long mascotaId) {
        Cliente cliente = obtenerCliente(clienteId);
        Mascota mascota = obtenerMascota(mascotaId);
        if (!mascota.getCliente().getId().equals(cliente.getId())) {
            throw new BadRequestException("La mascota " + mascota.getNombreMascota()
                    + " no pertenece al cliente seleccionado.");
        }
        return mascota;
    }

    /**
     * Busca un veterinario por id.
     *
     * @param medicoId id del veterinario.
     * @return veterinario encontrado.
     * @throws NotFoundException si no existe.
     */
    public Medico obtenerMedico(Long medicoId) {
        validarIdObligatorio(medicoId, "veterinario");
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new NotFoundException("El veterinario con ID " + medicoId + " no existe."));
    }

    /**
     * Busca una cita por id.
     *
     * @param citaId id de la cita.
     * @return cita encontrada.
     * @throws NotFoundException si no existe.
     */
    public CitaMedica obtenerCita(Long citaId) {
        validarIdObligatorio(citaId, "cita");
        return citaRepository.findById(citaId)
                .orElseThrow(() -> new NotFoundException("La cita con ID " + citaId + " no existe."));
    }

    /**
     * Verifica que una cita pertenezca a la mascota indicada.
     *
     * @param cita cita a revisar.
     * @param mascota mascota esperada.
     * @throws BadRequestException si la cita es de otra mascota.
     */
    public void validarCitaDeLaMascota(CitaMedica cita, Mascota mascota) {
        if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La cita seleccionada no pertenece a la mascota "
                    + mascota.getNombreMascota() + ".");
        }
    }
}
