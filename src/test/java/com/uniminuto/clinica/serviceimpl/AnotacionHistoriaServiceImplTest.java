package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.EstadoHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de las reglas de negocio de {@link AnotacionHistoriaServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class AnotacionHistoriaServiceImplTest {

    /**
     * Repositorio simulado de anotaciones.
     */
    @Mock
    private AnotacionHistoriaRepository anotacionRepository;

    /**
     * Validador simulado de relaciones.
     */
    @Mock
    private ValidadorRelaciones validador;

    /**
     * Servicio bajo prueba.
     */
    @InjectMocks
    private AnotacionHistoriaServiceImpl anotacionService;

    /**
     * Historia clínica abierta de hace tres días.
     */
    private HistoriaMedica historia;

    /**
     * Prepara los datos comunes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        Cita cita = DatosPrueba.cita(1L, DatosPrueba.mascota(10L, DatosPrueba.cliente(1L)), DatosPrueba.medico(5L),
                LocalDateTime.now().minusDays(3).truncatedTo(ChronoUnit.HOURS), EstadoCita.ATENDIDA);
        historia = DatosPrueba.historia(7L, cita);
    }

    /**
     * Construye una solicitud válida de anotación.
     *
     * @return solicitud de prueba.
     */
    private AnotacionHistoriaRq solicitudValida() {
        AnotacionHistoriaRq rq = new AnotacionHistoriaRq();
        rq.setHistoriaId(7L);
        rq.setMedicoId(5L);
        rq.setFechaAnotacion(LocalDateTime.now().minusHours(1));
        rq.setDescripcion("Control");
        rq.setObservacion("Come bien");
        rq.setDiagnosticoEvolucion("Evolución favorable");
        return rq;
    }

    @Test
    @DisplayName("Agrega una anotación a una historia abierta")
    void crearAnotacionValida() {
        when(validador.obtenerHistoria(7L)).thenReturn(historia);
        when(validador.obtenerMedico(5L)).thenReturn(DatosPrueba.medico(5L));

        MiRespuestaRS rs = anotacionService.crearAnotacion(solicitudValida());

        assertEquals(201, rs.getStatus());
        verify(anotacionRepository).save(any(AnotacionHistoria.class));
    }

    @Test
    @DisplayName("No se pueden agregar anotaciones a una historia cerrada")
    void crearAnotacionHistoriaCerrada() {
        historia.setEstado(EstadoHistoria.CERRADA);
        when(validador.obtenerHistoria(7L)).thenReturn(historia);
        assertThrows(ConflictException.class, () -> anotacionService.crearAnotacion(solicitudValida()));
    }

    @Test
    @DisplayName("La fecha de la anotación no puede ser anterior a la consulta")
    void crearAnotacionAntesDeLaConsulta() {
        when(validador.obtenerHistoria(7L)).thenReturn(historia);
        when(validador.obtenerMedico(5L)).thenReturn(DatosPrueba.medico(5L));
        AnotacionHistoriaRq rq = solicitudValida();
        rq.setFechaAnotacion(historia.getFechaConsulta().minusDays(1));
        assertThrows(BadRequestException.class, () -> anotacionService.crearAnotacion(rq));
    }

    @Test
    @DisplayName("La observación es obligatoria")
    void crearAnotacionSinObservacion() {
        AnotacionHistoriaRq rq = solicitudValida();
        rq.setObservacion(null);
        assertThrows(BadRequestException.class, () -> anotacionService.crearAnotacion(rq));
    }
}
