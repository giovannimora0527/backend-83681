package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CitaDisponibilidad;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CitaServiceImplTests {
    @Test void noPermiteCitasParaVeterinarioInactivo() {
        CitaRepository citas = mock(CitaRepository.class);
        MascotaRepository mascotas = mock(MascotaRepository.class);
        MedicoRepository medicos = mock(MedicoRepository.class);
        Medico doctor = medico(false);
        when(mascotas.findById(3)).thenReturn(Optional.of(new Mascota()));
        when(medicos.findById(9L)).thenReturn(Optional.of(doctor));
        CitaServiceImpl service = service(citas, mascotas, medicos, mock(CitaDisponibilidadRepository.class));

        assertThrows(BadRequestException.class, () -> service.crearCita(request()));
        verify(citas, never()).save(any());
    }

    @Test void rechazaSuperposicionDelVeterinario() {
        CitaRepository citas = mock(CitaRepository.class);
        MascotaRepository mascotas = mock(MascotaRepository.class);
        MedicoRepository medicos = mock(MedicoRepository.class);
        CitaDisponibilidadRepository horarios = mock(CitaDisponibilidadRepository.class);
        Medico doctor = medico(true);
        when(mascotas.findById(3)).thenReturn(Optional.of(new Mascota()));
        when(medicos.findById(9L)).thenReturn(Optional.of(doctor));
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        CitaDisponibilidad h = new CitaDisponibilidad(); h.setHoraInicio(LocalTime.of(9, 0)); h.setHoraFin(LocalTime.of(17, 0));
        when(horarios.findByMedicoIdAndDiaSemanaAndActivoTrue(eq(9L), anyInt())).thenReturn(List.of(h));
        when(citas.existeCruceMedico(eq(9L), any(), any(), isNull())).thenReturn(true);
        CitaServiceImpl service = service(citas, mascotas, medicos, horarios);
        CitaRq request = request(); request.setFechaHora(start);

        assertThrows(BadRequestException.class, () -> service.crearCita(request));
        verify(citas, never()).save(any());
    }

    private static CitaServiceImpl service(CitaRepository citas, MascotaRepository mascotas, MedicoRepository medicos,
                                            CitaDisponibilidadRepository horarios) {
        CitaServiceImpl service = new CitaServiceImpl();
        ReflectionTestUtils.setField(service, "citaRepository", citas);
        ReflectionTestUtils.setField(service, "mascotaRepository", mascotas);
        ReflectionTestUtils.setField(service, "medicoRepository", medicos);
        ReflectionTestUtils.setField(service, "disponibilidadRepository", horarios);
        ReflectionTestUtils.setField(service, "historialRepository", mock(CitaHistorialRepository.class));
        return service;
    }
    private static CitaRq request() {
        CitaRq r = new CitaRq(); r.setMascotaId(3); r.setMedicoId(9L);
        r.setFechaHora(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0));
        r.setMotivo("Control"); return r;
    }
    private static Medico medico(boolean activo) { Medico m = new Medico(); m.setId(9L); m.setEstado(activo); return m; }
}
