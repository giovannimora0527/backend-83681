package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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
 * Pruebas unitarias para {@link HistoriaMedicaServiceImpl}.
 * Cubre el Requerimiento 5 (sección historia médica) del parcial:
 * crear, listar con filtro de fechas y actualizar.
 */
@ExtendWith(MockitoExtension.class)
class HistoriaMedicaServiceImplTest {

    @Mock
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private HistoriaMedicaServiceImpl historiaMedicaService;

    private HistoriaMedicaRq historiaRqValida() {
        HistoriaMedicaRq rq = new HistoriaMedicaRq();
        rq.setDiagnostico("Otitis leve");
        rq.setTratamiento("Limpieza y antibiótico tópico por 7 días");
        rq.setMascotaId(1L);
        rq.setMedicoId(1L);
        return rq;
    }

    // ---------- crearHistoriaMedica ----------

    @Test
    @DisplayName("crearHistoriaMedica guarda la historia y retorna respuesta exitosa cuando los datos son válidos")
    void crearHistoriaMedica_conDatosValidos_debeGuardarYRetornarRespuestaExitosa() {
        HistoriaMedicaRq rq = historiaRqValida();
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(new Medico()));

        MiRespuestaRS respuesta = historiaMedicaService.crearHistoriaMedica(rq);

        assertEquals(200, respuesta.getStatus());
        verify(historiaMedicaRepository, times(1)).save(any(HistoriaMedica.class));
    }

    @Test
    @DisplayName("crearHistoriaMedica lanza BadRequestException si el diagnóstico está vacío")
    void crearHistoriaMedica_sinDiagnostico_debeLanzarExcepcion() {
        HistoriaMedicaRq rq = historiaRqValida();
        rq.setDiagnostico(" ");

        assertThrows(BadRequestException.class, () -> historiaMedicaService.crearHistoriaMedica(rq));
        verify(historiaMedicaRepository, never()).save(any(HistoriaMedica.class));
    }

    @Test
    @DisplayName("crearHistoriaMedica lanza BadRequestException si la mascota no existe")
    void crearHistoriaMedica_conMascotaInexistente_debeLanzarExcepcion() {
        HistoriaMedicaRq rq = historiaRqValida();
        when(mascotaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> historiaMedicaService.crearHistoriaMedica(rq));
        verify(historiaMedicaRepository, never()).save(any(HistoriaMedica.class));
    }

    // ---------- listarHistoriasPorFecha ----------

    @Test
    @DisplayName("listarHistoriasPorFecha retorna las historias del repositorio cuando el rango es válido")
    void listarHistoriasPorFecha_conFechasValidas_debeRetornarLista() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(30);
        LocalDateTime fin = LocalDateTime.now();
        when(historiaMedicaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(inicio, fin))
                .thenReturn(List.of(new HistoriaMedica()));

        List<HistoriaMedica> resultado = historiaMedicaService.listarHistoriasPorFecha(inicio, fin);

        assertEquals(1, resultado.size());
        verify(historiaMedicaRepository, times(1)).findByFechaCreacionBetweenOrderByFechaCreacionDesc(inicio, fin);
    }

    @Test
    @DisplayName("listarHistoriasPorFecha lanza BadRequestException si la fecha inicial es posterior a la final")
    void listarHistoriasPorFecha_conRangoInvertido_debeLanzarExcepcion() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin = LocalDateTime.now().minusDays(1);

        assertThrows(BadRequestException.class, () -> historiaMedicaService.listarHistoriasPorFecha(inicio, fin));
    }

    // ---------- actualizarHistoriaMedica ----------

    @Test
    @DisplayName("actualizarHistoriaMedica guarda los cambios cuando los datos son válidos")
    void actualizarHistoriaMedica_conDatosValidos_debeActualizarYRetornarRespuestaExitosa() {
        HistoriaMedicaRq rq = historiaRqValida();
        rq.setHistoriaId(1L);
        when(historiaMedicaRepository.findById(1L)).thenReturn(Optional.of(new HistoriaMedica()));
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(new Medico()));

        MiRespuestaRS respuesta = historiaMedicaService.actualizarHistoriaMedica(rq);

        assertEquals(200, respuesta.getStatus());
        verify(historiaMedicaRepository, times(1)).save(any(HistoriaMedica.class));
    }

    @Test
    @DisplayName("actualizarHistoriaMedica lanza BadRequestException si la historia no existe")
    void actualizarHistoriaMedica_conHistoriaInexistente_debeLanzarExcepcion() {
        HistoriaMedicaRq rq = historiaRqValida();
        rq.setHistoriaId(99L);
        when(historiaMedicaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> historiaMedicaService.actualizarHistoriaMedica(rq));
        verify(historiaMedicaRepository, never()).save(any(HistoriaMedica.class));
    }
}
