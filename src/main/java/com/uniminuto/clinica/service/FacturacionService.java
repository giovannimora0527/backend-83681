package com.uniminuto.clinica.service;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.*;
import java.util.List;
public interface FacturacionService {
    Factura crear(Long atencionId, FacturaRq request, Long usuarioId);
    Factura pagar(Long facturaId, FacturaPagoRq request, Long usuarioId);
    Factura anular(Long facturaId, String motivo, Long usuarioId);
    List<Factura> porCliente(Long clienteId);
    List<Factura> porMascota(Integer mascotaId);
    Object detalle(Long facturaId);
    byte[] pdf(Long facturaId);
}
