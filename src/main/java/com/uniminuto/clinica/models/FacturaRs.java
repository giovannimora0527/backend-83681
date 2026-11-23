package com.uniminuto.clinica.models;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class FacturaRs {
    private Long id;
    private Long citaId;
    private Boolean vistaPrevia;
    private ClinicaFacturaRs clinica;
    private String clienteNombre;
    private String clienteDocumento;
    private String clienteTelefono;
    private String mascotaNombre;
    private String mascotaEspecie;
    private String mascotaRaza;
    private Long medicoId;
    private String medicoNombre;
    private String especializacion;
    private LocalDateTime citaFecha;
    private String motivoConsulta;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuesto;
    private BigDecimal total;
    private BigDecimal pagado;
    private BigDecimal saldo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAnulacion;
    private String motivoAnulacion;
    private List<DetalleFacturaRs> detalles;
    private List<PagoFacturaRs> pagos;
}
