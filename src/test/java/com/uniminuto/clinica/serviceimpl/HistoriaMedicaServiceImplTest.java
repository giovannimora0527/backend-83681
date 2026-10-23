package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.EstadoHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de las reglas de negocio de {@link HistoriaMedicaServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class HistoriaMedicaServiceImplTest {

    /**
     * Repositorio simulado de historias.
     */
    @Mock
    private HistoriaMedicaRepository historiaRepository;

    /**
     * Validador simulado de relaciones.
     */
    @Mock
    private ValidadorRelaciones validador;

    /**
     * Servicio bajo prueba.
     */
    @InjectMocks
    private HistoriaMedicaServiceImpl historiaService;

    /**
     * Mascota de prueba.
     */
    private Mascota mascota;

    /**
     * Veterinario de prueba.
     */
    private Medico medico;

    /**
     * Cita atendida de hace dos días.
     */
    private Cita citaAtendida;

    /**
     * Prepara los datos comunes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        Cliente cliente = DatosPrueba.cliente(1L);
        mascota = DatosPrueba.mascota(10L, cliente);
        medico = DatosPrueba.medico(5L);
        citaAtendida = DatosPrueba.cita(3L, mascota, medico,
                LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.HOURS), EstadoCita.ATENDIDA);
    }

    /**
     * Construye una solicitud válida de historia clínica.
     *
     * @return solicitud de prueba.
     */
    private HistoriaMedicaRq solicitudValida() {
        HistoriaMedicaRq rq = new HistoriaMedicaRq();
        rq.setClienteId(1L);
        rq.setMascotaId(10L);
        rq.setCitaId(3L);
        rq.setMedicoId(5L);
        rq.setFechaConsulta(citaAtendida.getFechaCita().plusMinutes(10));
        rq.setMotivoConsulta("Vómito");
        rq.setDiagnostico("Gastroenteritis");
        return rq;
    }

    /**
     * Configura los simulacros de cliente, mascota, veterinario y cita.
     *
     * @param cita cita que retorna el validador.
     */
    private void simularRelaciones(Cita cita) {
        when(validador.obtenerMascotaDeCliente(10L, 1L)).thenReturn(mascota);
        when(validador.obtenerMedico(5L)).thenReturn(medico);
        when(validador.obtenerCita(3L)).thenReturn(cita);
    }

    @Test
    @DisplayName("Crea la historia clínica de una cita atendida")
    void crearHistoriaValida() {
        simularRelaciones(citaAtendida);
        when(historiaRepository.existsByCitaCitaId(3L)).thenReturn(false);

        MiRespuestaRS rs = historiaService.crearHistoria(solicitudValida());

        assertEquals(201, rs.getStatus());
        verify(historiaRepository).save(any(HistoriaMedica.class));
    }

    @Test
    @DisplayName("No permite crear historia si la cita no está atendida")
    void crearHistoriaCitaNoAtendida() {
        citaAtendida.setEstado(EstadoCita.CONFIRMADA);
        simularRelaciones(citaAtendida);
        assertThrows(ConflictException.class, () -> historiaService.crearHistoria(solicitudValida()));
        verify(historiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("No permite asociar una cita de otra mascota")
    void crearHistoriaCitaDeOtraMascota() {
        Cita citaOtraMascota = DatosPrueba.cita(3L, DatosPrueba.mascota(99L, DatosPrueba.cliente(1L)), medico,
                citaAtendida.getFechaCita(), EstadoCita.ATENDIDA);
        simularRelaciones(citaOtraMascota);
        assertThrows(BadRequestException.class, () -> historiaService.crearHistoria(solicitudValida()));
    }

    @Test
    @DisplayName("Una cita solo puede tener una historia clínica")
    void crearHistoriaDuplicada() {
        simularRelaciones(citaAtendida);
        when(historiaRepository.existsByCitaCitaId(3L)).thenReturn(true);
        assertThrows(ConflictException.class, () -> historiaService.crearHistoria(solicitudValida()));
    }

    @Test
    @DisplayName("La fecha de consulta no puede ser anterior a la cita")
    void crearHistoriaFechaAnteriorACita() {
        simularRelaciones(citaAtendida);
        when(historiaRepository.existsByCitaCitaId(3L)).thenReturn(false);
        HistoriaMedicaRq rq = solicitudValida();
        rq.setFechaConsulta(citaAtendida.getFechaCita().minusDays(1));
        assertThrows(BadRequestException.class, () -> historiaService.crearHistoria(rq));
    }

    @Test
    @DisplayName("El diagnóstico es obligatorio")
    void crearHistoriaSinDiagnostico() {
        HistoriaMedicaRq rq = solicitudValida();
        rq.setDiagnostico("  ");
        assertThrows(BadRequestException.class, () -> historiaService.crearHistoria(rq));
    }

    @Test
    @DisplayName("No se puede cerrar una historia ya cerrada")
    void cerrarHistoriaCerrada() {
        HistoriaMedica historia = DatosPrueba.historia(1L, citaAtendida);
        historia.setEstado(EstadoHistoria.CERRADA);
        when(validador.obtenerHistoria(1L)).thenReturn(historia);
        assertThrows(ConflictException.class, () -> historiaService.cerrarHistoria(1L));
    }
}
