package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link ValidadorRelaciones}: existencia de cliente y
 * mascota, y pertenencia de la mascota al cliente.
 */
@ExtendWith(MockitoExtension.class)
class ValidadorRelacionesTest {

    /**
     * Repositorio simulado de clientes.
     */
    @Mock
    private ClienteRepository clienteRepository;

    /**
     * Repositorio simulado de mascotas.
     */
    @Mock
    private MascotaRepository mascotaRepository;

    /**
     * Componente bajo prueba.
     */
    @InjectMocks
    private ValidadorRelaciones validador;

    @Test
    @DisplayName("Retorna la mascota cuando pertenece al cliente")
    void mascotaDelCliente() {
        Cliente cliente = DatosPrueba.cliente(1L);
        Mascota mascota = DatosPrueba.mascota(10L, cliente);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota));

        assertEquals(mascota, validador.obtenerMascotaDeCliente(10L, 1L));
    }

    @Test
    @DisplayName("Rechaza una mascota que pertenece a otro cliente")
    void mascotaDeOtroCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(DatosPrueba.cliente(1L)));
        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(DatosPrueba.mascota(10L, DatosPrueba.cliente(2L))));

        assertThrows(BadRequestException.class, () -> validador.obtenerMascotaDeCliente(10L, 1L));
    }

    @Test
    @DisplayName("Responde 404 si el cliente no existe")
    void clienteInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> validador.obtenerMascotaDeCliente(10L, 1L));
    }

    @Test
    @DisplayName("Responde 404 si la mascota no existe")
    void mascotaInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(DatosPrueba.cliente(1L)));
        when(mascotaRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> validador.obtenerMascotaDeCliente(10L, 1L));
    }

    @Test
    @DisplayName("Rechaza un cliente inactivo")
    void clienteInactivo() {
        Cliente cliente = DatosPrueba.cliente(1L);
        cliente.setActivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        assertThrows(BadRequestException.class, () -> validador.obtenerCliente(1L));
    }
}
