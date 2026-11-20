package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
class ReportesServiceImplTests {
    @Test void exportaInventarioComoXlsx(){
        var service=new ReportesServiceImpl(mock(MedicamentoRepository.class),mock(InventarioLoteRepository.class),mock(InventarioMovimientoRepository.class),
                mock(CitaRepository.class),mock(FacturaRepository.class),mock(FacturaDetalleRepository.class),mock(FacturaPagoRepository.class),mock(AtencionMedicaRepository.class));
        byte[] xlsx=service.excel("inventario",null,null,null);
        assertEquals('P',xlsx[0]);assertEquals('K',xlsx[1]);
    }
}
