package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link FormulaMedicaServiceImpl}.
 * Cubre el Requerimiento 1 del parcial: listar las fórmulas médicas
 * del inventario ordenadas de la más reciente a la más antigua.
 */
@ExtendWith(MockitoExtension.class)
class FormulaMedicaServiceImplTest {

    @Mock
    private FormulaMedicaRepository formulaMedicaRepository;

    @InjectMocks
    private FormulaMedicaServiceImpl formulaMedicaService;

    /**
     * El servicio debe delegar en el repositorio y retornar la lista
     * ya ordenada por fecha de creación descendente.
     */
    @Test
    @DisplayName("listarFormulasMedicas retorna la lista ordenada entregada por el repositorio")
    void listarFormulasMedicas_debeRetornarListaOrdenada() {
        FormulaMedica reciente = new FormulaMedica();
        reciente.setFormulaId(2L);
        reciente.setFechaCreacion(LocalDateTime.now());

        FormulaMedica antigua = new FormulaMedica();
        antigua.setFormulaId(1L);
        antigua.setFechaCreacion(LocalDateTime.now().minusDays(5));

        List<FormulaMedica> listaOrdenada = List.of(reciente, antigua);
        when(formulaMedicaRepository.findAllByOrderByFechaCreacionDesc()).thenReturn(listaOrdenada);

        List<FormulaMedica> resultado = formulaMedicaService.listarFormulasMedicas();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(reciente.getFormulaId(), resultado.get(0).getFormulaId());
        verify(formulaMedicaRepository, times(1)).findAllByOrderByFechaCreacionDesc();
    }

    /**
     * Si no hay fórmulas médicas registradas, el servicio debe retornar
     * una lista vacía en lugar de fallar.
     */
    @Test
    @DisplayName("listarFormulasMedicas retorna lista vacía cuando no hay fórmulas registradas")
    void listarFormulasMedicas_sinRegistros_debeRetornarListaVacia() {
        when(formulaMedicaRepository.findAllByOrderByFechaCreacionDesc()).thenReturn(List.of());

        List<FormulaMedica> resultado = formulaMedicaService.listarFormulasMedicas();

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }
}
