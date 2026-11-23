package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;

import java.util.List;

/**
 * Consultas de catálogo que alimentan los combos del frontend (cliente, mascota y veterinario).
 */
public interface CatalogoService {

    /** @return todos los clientes registrados. */
    List<Cliente> listarClientes();

    /**
     * @param id id del cliente.
     * @return cliente encontrado.
     */
    Cliente consultarCliente(Long id);

    /**
     * Mascotas de un cliente. Es el combo dependiente: solo se ofrecen mascotas de ese cliente.
     *
     * @param clienteId cliente seleccionado.
     * @return mascotas del cliente.
     */
    List<Mascota> listarMascotasDeCliente(Long clienteId);

    /**
     * @param id id de la mascota.
     * @return mascota encontrada.
     */
    Mascota consultarMascota(Integer id);

    /** @return todos los veterinarios registrados. */
    List<Medico> listarVeterinarios();
}
