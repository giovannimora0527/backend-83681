package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de las reglas de negocio de {@link FormulaMedicaServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class FormulaMedicaServiceImplTest {

    /**
     * Repositorio simulado de fórmulas.
     */
    @Mock
    private FormulaMedicaRepository formulaRepository;

    /**
     * Validador simulado de relaciones.
     */
    @Mock
    private ValidadorRelaciones validador;

    /**
     * Servicio bajo prueba.
     */
    @InjectMocks
    private FormulaMedicaServiceImpl formulaService;

    /**
     * Mascota de prueba.
     */
    private Mascota mascota;

    /**
     * Veterinario de prueba.
     */
    private Medico medico;

    /**
     * Historia clínica de la mascota (consulta de hace 5 días).
     */
    private HistoriaMedica historia;

    /**
     * Prepara los datos comunes de cada prueba.
     */
    @BeforeEach
    void setUp() {
        Cliente cliente = DatosPrueba.cliente(1L);
        mascota = DatosPrueba.mascota(10L, cliente);
        medico = DatosPrueba.medico(5L);
        Cita cita = DatosPrueba.cita(3L, mascota, medico,
                LocalDateTime.now().minusDays(5).truncatedTo(ChronoUnit.HOURS), EstadoCita.ATENDIDA);
        historia = DatosPrueba.historia(7L, cita);
    }

    /**
     * Construye una solicitud válida de fórmula.
     *
     * @return solicitud de prueba.
     */
    private FormulaMedicaRq solicitudValida() {
        FormulaMedicaRq rq = new FormulaMedicaRq();
        rq.setClienteId(1L);
        rq.setMascotaId(10L);
        rq.setHistoriaId(7L);
        rq.setMedicoId(5L);
        rq.setFechaEmision(LocalDate.now());
        rq.setMedicamento("Amoxicilina");
        rq.setDosis(new BigDecimal("250"));
        rq.setUnidadDosis("mg");
        rq.setFrecuenciaHoras(12);
        rq.setDuracionDias(7);
        rq.setIndicaciones("Dar con comida");
        return rq;
    }

    /**
     * Configura los simulacros de mascota, veterinario e historia.
     */
    private void simularRelaciones() {
        when(validador.obtenerMascotaDeCliente(10L, 1L)).thenReturn(mascota);
        when(validador.obtenerMedico(5L)).thenReturn(medico);
        when(validador.obtenerHistoria(7L)).thenReturn(historia);
    }

    @Test
    @DisplayName("Crea una fórmula válida y la deja activa")
    void crearFormulaValida() {
        simularRelaciones();
        when(formulaRepository.existeFormulaActiva(eq(7L), eq("Amoxicilina"), isNull())).thenReturn(false);

        MiRespuestaRS rs = formulaService.crearFormula(solicitudValida());

        assertEquals(201, rs.getStatus());
        verify(formulaRepository).save(any(FormulaMedica.class));
    }

    @Test
    @DisplayName("No asocia la fórmula a una consulta de otra mascota")
    void crearFormulaConsultaDeOtraMascota() {
        historia.setMascota(DatosPrueba.mascota(99L, DatosPrueba.cliente(2L)));
        simularRelaciones();
        assertThrows(BadRequestException.class, () -> formulaService.crearFormula(solicitudValida()));
        verify(formulaRepository, never()).save(any());
    }

    @Test
    @DisplayName("La fecha de emisión no puede ser anterior a la consulta")
    void crearFormulaFechaAnteriorConsulta() {
        simularRelaciones();
        FormulaMedicaRq rq = solicitudValida();
        rq.setFechaEmision(historia.getFechaConsulta().toLocalDate().minusDays(1));
        assertThrows(BadRequestException.class, () -> formulaService.crearFormula(rq));
    }

    @Test
    @DisplayName("La dosis debe ser mayor que cero")
    void crearFormulaDosisInvalida() {
        FormulaMedicaRq rq = solicitudValida();
        rq.setDosis(BigDecimal.ZERO);
        assertThrows(BadRequestException.class, () -> formulaService.crearFormula(rq));
    }

    @Test
    @DisplayName("La duración del tratamiento debe estar entre 1 y 365 días")
    void crearFormulaDuracionInvalida() {
        FormulaMedicaRq rq = solicitudValida();
        rq.setDuracionDias(0);
        assertThrows(BadRequestException.class, () -> formulaService.crearFormula(rq));
    }

    @Test
    @DisplayName("No permite dos fórmulas activas del mismo medicamento en la misma consulta")
    void crearFormulaDuplicada() {
        simularRelaciones();
        when(formulaRepository.existeFormulaActiva(eq(7L), eq("Amoxicilina"), isNull())).thenReturn(true);
        assertThrows(ConflictException.class, () -> formulaService.crearFormula(solicitudValida()));
    }

    @Test
    @DisplayName("Una fórmula inactiva no se puede editar")
    void editarFormulaInactiva() {
        FormulaMedica formula = new FormulaMedica();
        formula.setFormulaId(1L);
        formula.setActivo(false);
        when(formulaRepository.findById(1L)).thenReturn(Optional.of(formula));
        assertThrows(ConflictException.class, () -> formulaService.editarFormula(1L, solicitudValida()));
    }

    @Test
    @DisplayName("Inactiva una fórmula en lugar de eliminarla")
    void inactivarFormula() {
        FormulaMedica formula = new FormulaMedica();
        formula.setFormulaId(1L);
        formula.setActivo(true);
        when(formulaRepository.findById(1L)).thenReturn(Optional.of(formula));

        formulaService.inactivarFormula(1L);

        assertFalse(formula.getActivo());
        verify(formulaRepository).save(formula);
    }
}
