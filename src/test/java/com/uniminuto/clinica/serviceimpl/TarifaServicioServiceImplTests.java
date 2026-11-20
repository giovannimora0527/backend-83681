package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.TarifaServicio;
import com.uniminuto.clinica.models.TarifaServicioRq;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.TarifaServicioRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TarifaServicioServiceImplTests {
    @Test void cambioDeTarifaCierraLaVersionAnteriorYConservaElImporteNuevo() {
        TarifaServicioRepository repo = mock(TarifaServicioRepository.class);
        TarifaServicio anterior = new TarifaServicio(); anterior.setId(3L); anterior.setNombre("Consulta");
        anterior.setPrecio(new BigDecimal("50000"));
        when(repo.findLockedById(3L)).thenReturn(Optional.of(anterior));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var service = new TarifaServicioServiceImpl(repo, mock(EspecializacionRepository.class));

        TarifaServicio nueva = service.actualizar(3L, new TarifaServicioRq(null, "Consulta", "Control", new BigDecimal("60000"), new BigDecimal("19"), true));

        assertNotNull(anterior.getVigenteHasta());
        assertEquals(new BigDecimal("50000"), anterior.getPrecio());
        assertEquals(new BigDecimal("60000"), nueva.getPrecio());
        assertNull(nueva.getVigenteHasta());
        verify(repo, times(2)).save(any(TarifaServicio.class));
    }
}
