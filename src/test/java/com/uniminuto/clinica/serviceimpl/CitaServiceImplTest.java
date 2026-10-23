package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.CambioEstadoCitaRq;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReactivarCitaRq;
import com.uniminuto.clinica.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de las reglas de negocio de {@link CitaServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CitaServiceImplTest {

    /**
     * Repositorio simulado de citas.
     */
    @Mock
    private CitaRepository citaRepository;

    /**
     * Validador simulado de relaciones.
     */
    @Mock
    private ValidadorRelaciones validador;

    /**
     * Servicio bajo prueba.
     */
    @InjectMocks
    private CitaServiceImpl citaService;

    /**
     * Cliente de prueba.
     */
    private Cliente cliente;

    /**
     * Mascota de prueba (del cliente).
     */
    private Mascota mascota;

    /**
     * Veterinario de prueba.
     */
    private Medico medico;

    /**
     * Fecha futura usada en las citas de prueba.
     */
    private LocalDateTime manana;

    /**
     * Prepara los datos comunes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        cliente = DatosPrueba.cliente(1L);
        mascota = DatosPrueba.mascota(10L, cliente);
        medico = DatosPrueba.medico(5L);
        manana = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * Construye una solicitud válida de cita.
     *
     * @return solicitud de prueba.
     */
    private CitaRq solicitudValida() {
        CitaRq rq = new CitaRq();
        rq.setClienteId(1L);
        rq.setMascotaId(10L);
        rq.setMedicoId(5L);
        rq.setFechaCita(manana);
        rq.setMotivo("Control anual");
        rq.setEstado("PENDIENTE");
        return rq;
    }

    @Test
    @DisplayName("Crea la cita cuando todos los datos y reglas son válidos")
    void crearCitaValida() {
        when(validador.obtenerMascotaDeCliente(10L, 1L)).thenReturn(mascota);
        when(validador.obtenerMedico(5L)).thenReturn(medico);
        when(citaRepository.buscarCrucesMedico(eq(5L), any(), any(), isNull(), eq(EstadoCita.CANCELADA)))
                .thenReturn(List.of());
        when(citaRepository.buscarCrucesMascota(eq(10L), any(), any(), isNull(), eq(EstadoCita.CANCELADA)))
                .thenReturn(List.of());

        MiRespuestaRS rs = citaService.crearCita(solicitudValida());

        assertEquals(201, rs.getStatus());
        verify(citaRepository).save(any(Cita.class));
    }

    @Test
    @DisplayName("No permite crear una cita con estado ATENDIDA")
    void crearCitaEstadoInicialInvalido() {
        CitaRq rq = solicitudValida();
        rq.setEstado("ATENDIDA");
        assertThrows(BadRequestException.class, () -> citaService.crearCita(rq));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("No permite crear citas en fechas anteriores a la actual")
    void crearCitaFechaPasada() {
        CitaRq rq = solicitudValida();
        rq.setFechaCita(LocalDateTime.now().minusHours(2));
        when(validador.obtenerMascotaDeCliente(10L, 1L)).thenReturn(mascota);
        when(validador.obtenerMedico(5L)).thenReturn(medico);

        assertThrows(BadRequestException.class, () -> citaService.crearCita(rq));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rechaza la cita si el veterinario tiene otra cita a menos de 15 minutos")
    void crearCitaVeterinarioNoDisponible() {
        Cita otra = DatosPrueba.cita(99L, DatosPrueba.mascota(11L, cliente), medico, manana.plusMinutes(10),
                EstadoCita.PROGRAMADA);
        when(validador.obtenerMascotaDeCliente(10L, 1L)).thenReturn(mascota);
        when(validador.obtenerMedico(5L)).thenReturn(medico);
        when(citaRepository.buscarCrucesMedico(eq(5L), eq(manana.minusMinutes(15)), eq(manana.plusMinutes(15)),
                isNull(), eq(EstadoCita.CANCELADA))).thenReturn(List.of(otra));

        ConflictException ex = assertThrows(ConflictException.class, () -> citaService.crearCita(solicitudValida()));
        assertEquals(true, ex.getMessage().contains("no está disponible"));
    }

    @Test
    @DisplayName("Rechaza la cita si la mascota ya tiene otra cita simultánea")
    void crearCitaMascotaConCitaSimultanea() {
        Cita otra = DatosPrueba.cita(99L, mascota, DatosPrueba.medico(6L), manana, EstadoCita.PENDIENTE);
        when(validador.obtenerMascotaDeCliente(10L, 1L)).thenReturn(mascota);
        when(validador.obtenerMedico(5L)).thenReturn(medico);
        when(citaRepository.buscarCrucesMedico(any(), any(), any(), any(), any())).thenReturn(List.of());
        when(citaRepository.buscarCrucesMascota(eq(10L), any(), any(), isNull(), eq(EstadoCita.CANCELADA)))
                .thenReturn(List.of(otra));

        assertThrows(ConflictException.class, () -> citaService.crearCita(solicitudValida()));
    }

    @Test
    @DisplayName("Una cita cancelada no se puede editar")
    void editarCitaCancelada() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.CANCELADA);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        assertThrows(ConflictException.class, () -> citaService.editarCita(1L, solicitudValida()));
    }

    @Test
    @DisplayName("Una cita atendida no se puede editar")
    void editarCitaAtendida() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana.minusDays(3), EstadoCita.ATENDIDA);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        assertThrows(ConflictException.class, () -> citaService.editarCita(1L, solicitudValida()));
    }

    @Test
    @DisplayName("No se puede cambiar la mascota de una cita al editarla")
    void editarCitaCambiandoMascota() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.PENDIENTE);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        CitaRq rq = solicitudValida();
        rq.setMascotaId(20L);
        assertThrows(BadRequestException.class, () -> citaService.editarCita(1L, rq));
    }

    @Test
    @DisplayName("Una cita pendiente puede cambiar de fecha y veterinario")
    void editarCitaPendiente() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.PENDIENTE);
        Medico otroMedico = DatosPrueba.medico(6L);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        when(validador.obtenerMedico(6L)).thenReturn(otroMedico);
        when(citaRepository.buscarCrucesMedico(eq(6L), any(), any(), eq(1L), any())).thenReturn(List.of());
        when(citaRepository.buscarCrucesMascota(eq(10L), any(), any(), eq(1L), any())).thenReturn(List.of());

        CitaRq rq = solicitudValida();
        rq.setMedicoId(6L);
        rq.setFechaCita(manana.plusHours(2));
        citaService.editarCita(1L, rq);

        assertEquals(otroMedico, cita.getMedico());
        assertEquals(manana.plusHours(2), cita.getFechaCita());
    }

    @Test
    @DisplayName("No permite transiciones de estado inválidas (CONFIRMADA -> PENDIENTE)")
    void cambiarEstadoTransicionInvalida() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.CONFIRMADA);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        CambioEstadoCitaRq rq = new CambioEstadoCitaRq();
        rq.setEstado("PENDIENTE");
        assertThrows(ConflictException.class, () -> citaService.cambiarEstado(1L, rq));
    }

    @Test
    @DisplayName("No permite marcar como atendida una cita de un día futuro")
    void atenderCitaFutura() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana.plusDays(1), EstadoCita.CONFIRMADA);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        CambioEstadoCitaRq rq = new CambioEstadoCitaRq();
        rq.setEstado("ATENDIDA");
        assertThrows(ConflictException.class, () -> citaService.cambiarEstado(1L, rq));
    }

    @Test
    @DisplayName("Una cita cancelada no cambia de estado sin reactivarla")
    void cambiarEstadoCitaCancelada() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.CANCELADA);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        CambioEstadoCitaRq rq = new CambioEstadoCitaRq();
        rq.setEstado("CONFIRMADA");
        assertThrows(ConflictException.class, () -> citaService.cambiarEstado(1L, rq));
    }

    @Test
    @DisplayName("Confirma una cita pendiente")
    void confirmarCitaPendiente() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.PENDIENTE);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        CambioEstadoCitaRq rq = new CambioEstadoCitaRq();
        rq.setEstado("confirmada");
        citaService.cambiarEstado(1L, rq);
        assertEquals(EstadoCita.CONFIRMADA, cita.getEstado());
    }

    @Test
    @DisplayName("Reactivar solo aplica a citas canceladas")
    void reactivarCitaNoCancelada() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.PENDIENTE);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        assertThrows(ConflictException.class, () -> citaService.reactivarCita(1L, new ReactivarCitaRq()));
    }

    @Test
    @DisplayName("Reactiva una cita cancelada y la deja PENDIENTE")
    void reactivarCitaCancelada() {
        Cita cita = DatosPrueba.cita(1L, mascota, medico, manana, EstadoCita.CANCELADA);
        when(validador.obtenerCita(1L)).thenReturn(cita);
        when(citaRepository.buscarCrucesMedico(any(), any(), any(), eq(1L), any())).thenReturn(List.of());
        when(citaRepository.buscarCrucesMascota(any(), any(), any(), eq(1L), any())).thenReturn(List.of());

        citaService.reactivarCita(1L, new ReactivarCitaRq());
        assertEquals(EstadoCita.PENDIENTE, cita.getEstado());
    }
}
