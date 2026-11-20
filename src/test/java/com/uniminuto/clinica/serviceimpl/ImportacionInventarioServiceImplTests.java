package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioImportacion;
import com.uniminuto.clinica.repository.InventarioImportacionRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.ImportacionFilaProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ImportacionInventarioServiceImplTests {
    @Test void prevalidacionAceptaMasDeCincuentaFilasValidas() {
        InventarioImportacionRepository logs = mock(InventarioImportacionRepository.class);
        MedicamentoRepository productos = mock(MedicamentoRepository.class);
        when(productos.existsByNombreIgnoreCase(any())).thenReturn(false);
        when(productos.existsByCodigo(any())).thenReturn(false);
        when(logs.save(any())).thenAnswer(inv -> { InventarioImportacion e = inv.getArgument(0); e.setId(12L); return e; });
        var service = new ImportacionInventarioServiceImpl(logs, productos, mock(ImportacionFilaProcessor.class), new ObjectMapper());
        StringBuilder csv = new StringBuilder("nombre,tipoProducto,registroInvima,precioCompra,precioVenta,stockMinimo,numeroLote,fechaIngreso,fechaVencimiento,cantidad\n");
        for (int i = 1; i <= 60; i++) csv.append("Producto ").append(i).append(",MEDICAMENTO,INV-").append(i)
                .append(",100,150,5,L-").append(i).append(",2026-11-17,2027-11-17,10\n");
        MockMultipartFile file = new MockMultipartFile("archivo", "inventario.csv", "text/csv", csv.toString().getBytes(StandardCharsets.UTF_8));

        var result = service.prevalidar(file, 3L);

        assertEquals(60, result.totalFilas());
        assertEquals(60, result.validas());
        assertEquals(0, result.invalidas());
        verify(logs).save(any(InventarioImportacion.class));
    }
}
