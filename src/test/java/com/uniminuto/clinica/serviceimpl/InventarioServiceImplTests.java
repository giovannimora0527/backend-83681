package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.InventarioMovimiento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EntradaLoteRq;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.InventarioMovimientoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventarioServiceImplTests {
    @Test void ingresoRegistraLoteYMovimientoEnLaMismaOperacion() {
        MedicamentoRepository medicamentos = mock(MedicamentoRepository.class);
        InventarioLoteRepository lotes = mock(InventarioLoteRepository.class);
        InventarioMovimientoRepository movimientos = mock(InventarioMovimientoRepository.class);
        Medicamento producto = new Medicamento(); producto.setId(8L);
        when(medicamentos.findById(8L)).thenReturn(Optional.of(producto));
        when(lotes.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var service = new InventarioServiceImpl(medicamentos, lotes, movimientos);
        var request = new EntradaLoteRq(8L, "L-01", LocalDate.now(), LocalDate.now().plusMonths(6),
                new BigDecimal("25"), new BigDecimal("100"), new BigDecimal("150"), "OC-5", 4L);

        InventarioLote result = service.registrarEntrada(request);

        assertEquals(new BigDecimal("25"), result.getCantidadDisponible());
        verify(lotes).save(any(InventarioLote.class));
        verify(movimientos).save(argThat(m -> "ENTRADA".equals(m.getTipo()) && m.getCantidad().compareTo(new BigDecimal("25")) == 0));
    }

    @Test void ingresoRechazaCantidadCeroAntesDeConsultarElProducto() {
        MedicamentoRepository medicamentos = mock(MedicamentoRepository.class);
        var service = new InventarioServiceImpl(medicamentos, mock(InventarioLoteRepository.class), mock(InventarioMovimientoRepository.class));
        var request = new EntradaLoteRq(8L, "L-01", LocalDate.now(), LocalDate.now().plusDays(1),
                BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, null, 4L);
        assertThrows(BadRequestException.class, () -> service.registrarEntrada(request));
        verifyNoInteractions(medicamentos);
    }
}
