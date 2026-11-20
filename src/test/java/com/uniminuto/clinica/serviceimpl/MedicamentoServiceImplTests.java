package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ProductoRq;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MedicamentoServiceImplTests {
    @Test void altaGeneraCodigoDeOchoCaracteresYConservaDatosDelCatalogo() {
        MedicamentoRepository productos = mock(MedicamentoRepository.class);
        when(productos.existsByCodigo(anyString())).thenReturn(false);
        when(productos.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var service = new MedicamentoServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "medicamentoRepository", productos);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "inventarioLoteRepository", mock(InventarioLoteRepository.class));
        var rq = new ProductoRq("Antiparasitario", null, "Tableta", null, "MEDICAMENTO", "INV-1",
                new BigDecimal("1000"), new BigDecimal("1500"), new BigDecimal("5"), null, true);

        Medicamento saved = service.crear(rq);

        assertTrue(saved.getCodigo().matches("[A-Z0-9]{8}"));
        assertEquals("INV-1", saved.getRegistroInvima());
        assertEquals(new BigDecimal("1500"), saved.getPrecioVenta());
    }

    @Test void rechazaMedicamentoSinRegistroInvima() {
        MedicamentoRepository productos = mock(MedicamentoRepository.class);
        var service = new MedicamentoServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(service, "medicamentoRepository", productos);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "inventarioLoteRepository", mock(InventarioLoteRepository.class));
        var rq = new ProductoRq("Antiparasitario", null, null, null, "MEDICAMENTO", null,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, null, true);
        assertThrows(BadRequestException.class, () -> service.crear(rq));
        verifyNoInteractions(productos);
    }
}
