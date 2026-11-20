package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.models.FacturaPagoRq;
import com.uniminuto.clinica.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FacturacionServiceImplTests {
    @Test void pagosParcialesActualizanEstadoSinSuperarElTotal(){
        FacturaRepository facturas=mock(FacturaRepository.class);FacturaPagoRepository pagos=mock(FacturaPagoRepository.class);
        Factura factura=new Factura();factura.setId(9L);factura.setTotal(new BigDecimal("100.00"));factura.setEstado("PENDIENTE");
        when(facturas.findLockedById(9L)).thenReturn(Optional.of(factura));when(facturas.save(any())).thenAnswer(i->i.getArgument(0));
        when(pagos.totalByFactura(9L)).thenReturn(BigDecimal.ZERO,new BigDecimal("40.00"));
        FacturacionServiceImpl service=new FacturacionServiceImpl(facturas,mock(FacturaDetalleRepository.class),pagos,
                mock(AtencionMedicaRepository.class),mock(CitaRepository.class),mock(FormulaDetalleRepository.class),
                mock(DespachoDetalleRepository.class),mock(TarifaServicioRepository.class),new tools.jackson.databind.ObjectMapper(),"Clínica","","","","");
        service.pagar(9L,new FacturaPagoRq(new BigDecimal("40"),"EFECTIVO",null),3L);assertEquals("PARCIAL",factura.getEstado());
        service.pagar(9L,new FacturaPagoRq(new BigDecimal("60"),"TRANSFERENCIA",null),3L);assertEquals("PAGADA",factura.getEstado());
    }
}
