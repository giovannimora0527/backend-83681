package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link CitaServiceImpl}.
 * Cubre los Requerimientos 2/3 (filtrar por fecha) y 4 (crear/actualizar
 * citas) del parcial, incluyendo el control de errores exigido en la
 * rúbrica "Servicios funcionando".
 */
@ExtendWith(MockitoExtension.class)
class CitaServiceImplTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private CitaServiceImpl citaService;

    private CitaRq citaRqValida() {
        CitaRq rq = new CitaRq();
        rq.setFechaCita(LocalDateTime.now().plusDays(1));
        rq.setMotivo("Control anual");
        rq.setEstado("PROGRAMADA");
        rq.setMascotaId(1L);
        rq.setMedicoId(1L);
        return rq;
    }

    // ---------- filtrarCitasPorFecha ----------

    @Test
    @DisplayName("filtrarCitasPorFecha retorna las citas del repositorio cuando el rango es válido")
    void filtrarCitasPorFecha_conFechasValidas_debeRetornarLista() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(10);
        LocalDateTime fin = LocalDateTime.now();
        Cita cita = new Cita();
        cita.setCitaId(1L);

        when(citaRepository.findByFechaCitaBetweenOrderByFechaCitaDesc(inicio, fin)).thenReturn(List.of(cita));

        List<Cita> resultado = citaService.filtrarCitasPorFecha(inicio, fin);

        assertEquals(1, resultado.size());
        verify(citaRepository, times(1)).findByFechaCitaBetweenOrderByFechaCitaDesc(inicio, fin);
    }

    @Test
    @DisplayName("filtrarCitasPorFecha lanza BadRequestException si alguna fecha es nula")
    void filtrarCitasPorFecha_conFechaNula_debeLanzarExcepcion() {
        assertThrows(BadRequestException.class, () -> citaService.filtrarCitasPorFecha(null, LocalDateTime.now()));
    }

    @Test
    @DisplayName("filtrarCitasPorFecha lanza BadRequestException si la fecha inicial es posterior a la final")
    void filtrarCitasPorFecha_conRangoInvertido_debeLanzarExcepcion() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin = LocalDateTime.now().minusDays(1);

        assertThrows(BadRequestException.class, () -> citaService.filtrarCitasPorFecha(inicio, fin));
    }

    // ---------- crearCita ----------

    @Test
    @DisplayName("crearCita guarda la cita y retorna respuesta exitosa cuando los datos son válidos")
    void crearCita_conDatosValidos_debeGuardarYRetornarRespuestaExitosa() {
        CitaRq rq = citaRqValida();
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(new Medico()));

        MiRespuestaRS respuesta = citaService.crearCita(rq);

        assertNotNull(respuesta);
        assertEquals(200, respuesta.getStatus());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("crearCita lanza BadRequestException si el motivo está vacío")
    void crearCita_sinMotivo_debeLanzarExcepcion() {
        CitaRq rq = citaRqValida();
        rq.setMotivo("  ");

        assertThrows(BadRequestException.class, () -> citaService.crearCita(rq));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("crearCita lanza BadRequestException si la mascota no existe")
    void crearCita_conMascotaInexistente_debeLanzarExcepcion() {
        CitaRq rq = citaRqValida();
        when(mascotaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> citaService.crearCita(rq));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    @DisplayName("crearCita lanza BadRequestException si el médico no existe")
    void crearCita_conMedicoInexistente_debeLanzarExcepcion() {
        CitaRq rq = citaRqValida();
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> citaService.crearCita(rq));
        verify(citaRepository, never()).save(any(Cita.class));
    }

    // ---------- actualizarCita ----------

    @Test
    @DisplayName("actualizarCita guarda los cambios y retorna respuesta exitosa cuando los datos son válidos")
    void actualizarCita_conDatosValidos_debeActualizarYRetornarRespuestaExitosa() {
        CitaRq rq = citaRqValida();
        rq.setCitaId(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(new Cita()));
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(new Mascota()));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(new Medico()));

        MiRespuestaRS respuesta = citaService.actualizarCita(rq);

        assertEquals(200, respuesta.getStatus());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("actualizarCita lanza BadRequestException si no se envía el ID de la cita")
    void actualizarCita_sinCitaId_debeLanzarExcepcion() {
        CitaRq rq = citaRqValida();

        assertThrows(BadRequestException.class, () -> citaService.actualizarCita(rq));
    }

    @Test
    @DisplayName("actualizarCita lanza BadRequestException si la cita no existe")
    void actualizarCita_conCitaInexistente_debeLanzarExcepcion() {
        CitaRq rq = citaRqValida();
        rq.setCitaId(99L);
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> citaService.actualizarCita(rq));
        verify(citaRepository, never()).save(any(Cita.class));
    }
}
