package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link AnotacionHistoriaServiceImpl}.
 * Cubre el Requerimiento 5 (sección anotación_historia) del parcial:
 * crear, listar y actualizar anotaciones de una historia médica.
 */
@ExtendWith(MockitoExtension.class)
class AnotacionHistoriaServiceImplTest {

    @Mock
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Mock
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private AnotacionHistoriaServiceImpl anotacionHistoriaService;

    private AnotacionHistoriaRq anotacionRqValida() {
        AnotacionHistoriaRq rq = new AnotacionHistoriaRq();
        rq.setObservacion("Paciente responde bien al tratamiento");
        rq.setHistoriaId(1L);
        rq.setMedicoId(1L);
        return rq;
    }

    // ---------- crearAnotacion ----------

    @Test
    @DisplayName("crearAnotacion guarda la anotación y retorna respuesta exitosa cuando los datos son válidos")
    void crearAnotacion_conDatosValidos_debeGuardarYRetornarRespuestaExitosa() {
        AnotacionHistoriaRq rq = anotacionRqValida();
        when(historiaMedicaRepository.findById(1L)).thenReturn(Optional.of(new HistoriaMedica()));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(new Medico()));

        MiRespuestaRS respuesta = anotacionHistoriaService.crearAnotacion(rq);

        assertEquals(200, respuesta.getStatus());
        verify(anotacionHistoriaRepository, times(1)).save(any(AnotacionHistoria.class));
    }

    @Test
    @DisplayName("crearAnotacion lanza BadRequestException si la observación está vacía")
    void crearAnotacion_sinObservacion_debeLanzarExcepcion() {
        AnotacionHistoriaRq rq = anotacionRqValida();
        rq.setObservacion("");

        assertThrows(BadRequestException.class, () -> anotacionHistoriaService.crearAnotacion(rq));
        verify(anotacionHistoriaRepository, never()).save(any(AnotacionHistoria.class));
    }

    @Test
    @DisplayName("crearAnotacion lanza BadRequestException si la historia médica no existe")
    void crearAnotacion_conHistoriaInexistente_debeLanzarExcepcion() {
        AnotacionHistoriaRq rq = anotacionRqValida();
        when(historiaMedicaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> anotacionHistoriaService.crearAnotacion(rq));
        verify(anotacionHistoriaRepository, never()).save(any(AnotacionHistoria.class));
    }

    // ---------- listarAnotacionesPorHistoria ----------

    @Test
    @DisplayName("listarAnotacionesPorHistoria retorna las anotaciones cuando la historia existe")
    void listarAnotacionesPorHistoria_conHistoriaExistente_debeRetornarLista() {
        when(historiaMedicaRepository.findById(1L)).thenReturn(Optional.of(new HistoriaMedica()));
        when(anotacionHistoriaRepository.findByHistoriaMedica_HistoriaIdOrderByFechaCreacionDesc(1L))
                .thenReturn(List.of(new AnotacionHistoria()));

        List<AnotacionHistoria> resultado = anotacionHistoriaService.listarAnotacionesPorHistoria(1L);

        assertEquals(1, resultado.size());
        verify(anotacionHistoriaRepository, times(1)).findByHistoriaMedica_HistoriaIdOrderByFechaCreacionDesc(1L);
    }

    @Test
    @DisplayName("listarAnotacionesPorHistoria lanza BadRequestException si la historia no existe")
    void listarAnotacionesPorHistoria_conHistoriaInexistente_debeLanzarExcepcion() {
        when(historiaMedicaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> anotacionHistoriaService.listarAnotacionesPorHistoria(99L));
    }

    // ---------- actualizarAnotacion ----------

    @Test
    @DisplayName("actualizarAnotacion guarda los cambios cuando los datos son válidos")
    void actualizarAnotacion_conDatosValidos_debeActualizarYRetornarRespuestaExitosa() {
        AnotacionHistoriaRq rq = anotacionRqValida();
        rq.setAnotacionId(1L);
        when(anotacionHistoriaRepository.findById(1L)).thenReturn(Optional.of(new AnotacionHistoria()));
        when(historiaMedicaRepository.findById(1L)).thenReturn(Optional.of(new HistoriaMedica()));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(new Medico()));

        MiRespuestaRS respuesta = anotacionHistoriaService.actualizarAnotacion(rq);

        assertEquals(200, respuesta.getStatus());
        verify(anotacionHistoriaRepository, times(1)).save(any(AnotacionHistoria.class));
    }

    @Test
    @DisplayName("actualizarAnotacion lanza BadRequestException si la anotación no existe")
    void actualizarAnotacion_conAnotacionInexistente_debeLanzarExcepcion() {
        AnotacionHistoriaRq rq = anotacionRqValida();
        rq.setAnotacionId(99L);
        when(anotacionHistoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> anotacionHistoriaService.actualizarAnotacion(rq));
        verify(anotacionHistoriaRepository, never()).save(any(AnotacionHistoria.class));
    }
}
