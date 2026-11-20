package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ConfirmarDespachoRq;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DespachoServiceImplTests {
    @Test void noDispensaMasDeLoPrescritoNiTocaInventario() {
        DespachoRepository despachos = mock(DespachoRepository.class);
        FormulaDetalleRepository formulas = mock(FormulaDetalleRepository.class);
        InventarioLoteRepository lotes = mock(InventarioLoteRepository.class);
        when(despachos.findByIdempotencyKey("req-1")).thenReturn(Optional.empty());
        when(despachos.saveAndFlush(any())).thenAnswer(inv -> {
            Despacho d = inv.getArgument(0); d.setId(10L); return d;
        });
        FormulaDetalle formula = new FormulaDetalle();
        formula.setCantidadPrescrita(new BigDecimal("5")); formula.setCantidadDispensada(BigDecimal.ZERO);
        Medicamento medicamento = new Medicamento(); medicamento.setId(7L); formula.setMedicamento(medicamento);
        when(formulas.findLockedById(2L)).thenReturn(Optional.of(formula));
        var service = new DespachoServiceImpl(despachos, mock(DespachoDetalleRepository.class), formulas,
                lotes, mock(InventarioMovimientoRepository.class), mock(DespachoHistorialRepository.class), mock(DespachoReversionRepository.class), mock(FacturaDetalleRepository.class),mock(AtencionMedicaRepository.class),mock(CitaRepository.class));
        var rq = new ConfirmarDespachoRq("req-1", List.of(new ConfirmarDespachoRq.Detalle(2L, new BigDecimal("6"))));

        assertThrows(BadRequestException.class, () -> service.confirmar(rq, 3L));
        verifyNoInteractions(lotes);
    }

    @Test void reintentoConLaMismaClaveYContenidoDistintoSeRechaza() {
        DespachoRepository despachos = mock(DespachoRepository.class);
        Despacho existing = new Despacho(); existing.setRequestHash("hash-distinto");
        when(despachos.findByIdempotencyKey("req-2")).thenReturn(Optional.of(existing));
        var service = new DespachoServiceImpl(despachos, mock(DespachoDetalleRepository.class), mock(FormulaDetalleRepository.class),
                mock(InventarioLoteRepository.class), mock(InventarioMovimientoRepository.class), mock(DespachoHistorialRepository.class), mock(DespachoReversionRepository.class), mock(FacturaDetalleRepository.class),mock(AtencionMedicaRepository.class),mock(CitaRepository.class));
        var rq = new ConfirmarDespachoRq("req-2", List.of(new ConfirmarDespachoRq.Detalle(2L, BigDecimal.ONE)));

        assertThrows(BadRequestException.class, () -> service.confirmar(rq, 3L));
        verify(despachos, never()).saveAndFlush(any());
    }

    @Test void reversoRechazaDetalleYaFacturado(){
        DespachoRepository despachos=mock(DespachoRepository.class);Despacho despacho=new Despacho();despacho.setId(8L);despacho.setEstado("DESPACHADO");
        when(despachos.findLockedById(8L)).thenReturn(Optional.of(despacho));
        DespachoDetalle detalle=new DespachoDetalle();detalle.setId(11L);
        DespachoDetalleRepository lineas=mock(DespachoDetalleRepository.class);when(lineas.findByDespachoIdOrderByIdAsc(8L)).thenReturn(List.of(detalle));
        FacturaDetalleRepository facturadas=mock(FacturaDetalleRepository.class);when(facturadas.existeEnFacturaActiva(11L)).thenReturn(true);
        DespachoServiceImpl service=new DespachoServiceImpl(despachos,lineas,mock(FormulaDetalleRepository.class),mock(InventarioLoteRepository.class),
                mock(InventarioMovimientoRepository.class),mock(DespachoHistorialRepository.class),mock(DespachoReversionRepository.class),facturadas,mock(AtencionMedicaRepository.class),mock(CitaRepository.class));
        var error=assertThrows(BadRequestException.class,()->service.revertir(8L,"motivo",2L));
        assertEquals(true,error.getMessage().contains("factura"));
    }
}
