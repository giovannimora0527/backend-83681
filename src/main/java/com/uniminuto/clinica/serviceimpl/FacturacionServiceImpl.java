package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.FacturacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FacturacionServiceImpl implements FacturacionService {
    private final FacturaRepository facturas; private final FacturaDetalleRepository detalles;
    private final FacturaPagoRepository pagos; private final AtencionMedicaRepository atenciones;
    private final CitaRepository citas; private final FormulaDetalleRepository formulas;
    private final DespachoDetalleRepository detallesDespacho; private final TarifaServicioRepository tarifas;
    private final ObjectMapper mapper; private final String nombreClinica; private final String nitClinica; private final String direccionClinica; private final String telefonoClinica; private final String correoClinica;
    public FacturacionServiceImpl(FacturaRepository facturas, FacturaDetalleRepository detalles, FacturaPagoRepository pagos,
            AtencionMedicaRepository atenciones, CitaRepository citas, FormulaDetalleRepository formulas,
            DespachoDetalleRepository detallesDespacho, TarifaServicioRepository tarifas, ObjectMapper mapper,
            @Value("${app.clinic.name:Clínica Veterinaria}") String nombreClinica,
            @Value("${app.clinic.tax-id:}") String nitClinica, @Value("${app.clinic.address:}") String direccionClinica,
            @Value("${app.clinic.phone:}") String telefonoClinica, @Value("${app.clinic.email:}") String correoClinica) {
        this.facturas=facturas; this.detalles=detalles; this.pagos=pagos; this.atenciones=atenciones; this.citas=citas;
        this.formulas=formulas; this.detallesDespacho=detallesDespacho; this.tarifas=tarifas; this.mapper=mapper; this.nombreClinica=nombreClinica;
        this.nitClinica=nitClinica;this.direccionClinica=direccionClinica;this.telefonoClinica=telefonoClinica;this.correoClinica=correoClinica;
    }

    @Override @Transactional
    public Factura crear(Long atencionId, FacturaRq rq, Long usuarioId) {
        if (rq == null || rq.tarifaServicioId() == null) throw new BadRequestException("Debe indicar la tarifa del servicio prestado.");
        if (facturas.findByAtencionId(atencionId).isPresent()) throw new BadRequestException("Esta atención ya tiene factura.");
        AtencionMedica atencion=atenciones.findById(atencionId).orElseThrow(()->new BadRequestException("La atención no existe."));
        Cita cita=citas.findById(atencion.getCitaId()).orElseThrow(()->new BadRequestException("La cita de la atención no existe."));
        TarifaServicio tarifa=tarifas.findById(rq.tarifaServicioId()).orElseThrow(()->new BadRequestException("La tarifa no existe."));
        if (!Boolean.TRUE.equals(tarifa.getActivo()) || tarifa.getVigenteHasta()!=null) throw new BadRequestException("La tarifa no está vigente.");
        if (tarifa.getEspecializacion()!=null && !tarifa.getEspecializacion().getId().equals(cita.getMedico().getEspecializacion().getId()))
            throw new BadRequestException("La tarifa no corresponde a la especialidad del veterinario.");
        BigDecimal descuento=money(rq.descuento()==null?BigDecimal.ZERO:rq.descuento());
        if (descuento.signum()<0) throw new BadRequestException("El descuento no puede ser negativo.");
        if (descuento.signum()>0 && (rq.motivoDescuento()==null || rq.motivoDescuento().isBlank() || rq.motivoDescuento().length()>500))
            throw new BadRequestException("El descuento requiere un motivo de autorización de máximo 500 caracteres.");
        BigDecimal subtotal=tarifa.getPrecio().setScale(2,RoundingMode.HALF_UP);
        BigDecimal impuestos=subtotal.multiply(tarifa.getIvaPorcentaje()).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
        List<FacturaDetalle> lineas=new ArrayList<>();
        FacturaDetalle servicio=linea("SERVICIO",tarifa.getNombre(),null,null,null,BigDecimal.ONE,tarifa.getPrecio(),tarifa.getIvaPorcentaje()); lineas.add(servicio);
        for (FormulaDetalle formula:formulas.findByAtencionIdOrderByIdAsc(atencionId)) {
            for (DespachoDetalle despacho:detallesDespacho.findByFormulaDetalleIdOrderByIdAsc(formula.getId())) {
                if ("CANCELADO".equals(despacho.getDespacho().getEstado())) continue;
                if (detalles.existsByDespachoDetalleId(despacho.getId())) continue;
                BigDecimal cantidad=despacho.getCantidad(); BigDecimal precio=despacho.getLote().getPrecioVentaUnitario();
                FacturaDetalle d=linea("MEDICAMENTO",formula.getMedicamento().getNombre()+" / lote "+despacho.getLote().getNumeroLote(),
                    formula.getMedicamento().getId(),despacho.getLote().getId(),despacho.getId(),cantidad,precio,BigDecimal.ZERO);
                lineas.add(d); subtotal=subtotal.add(d.getSubtotal());
            }
        }
        if (descuento.compareTo(subtotal.add(impuestos))>0) throw new BadRequestException("El descuento no puede superar el valor antes de descuento.");
        BigDecimal total=subtotal.add(impuestos).subtract(descuento).setScale(2,RoundingMode.HALF_UP);
        Factura f=new Factura(); f.setNumero("FAC-"+UUID.randomUUID()); f.setAtencionId(atencionId);
        f.setClienteId(cita.getCliente().getId()); f.setMascotaId(cita.getMascota().getMascotaId()); f.setMedicoId(cita.getMedico().getId());
        f.setClinicaSnapshot(json(Map.of("nombre",nombreClinica,"identificacion",nitClinica,"direccion",direccionClinica,"telefono",telefonoClinica,"correo",correoClinica))); f.setPropietarioSnapshot(json(Map.of("nombres",cita.getCliente().getNombres(),"apellidos",cita.getCliente().getApellidos(),"documento",cita.getCliente().getNumeroDocumento(),"telefono",Objects.toString(cita.getCliente().getTelefono(),""))));
        f.setMascotaSnapshot(json(Map.of("nombre",Objects.toString(cita.getMascota().getNombreMascota(),""),"id",cita.getMascota().getMascotaId())));
        f.setMedicoSnapshot(json(Map.of("nombres",cita.getMedico().getNombres(),"apellidos",cita.getMedico().getApellidos(),"registro",cita.getMedico().getRegistroProfesional(),"especialidad",Objects.toString(cita.getMedico().getEspecializacion().getNombre(),""))));
        f.setSubtotal(subtotal.setScale(2,RoundingMode.HALF_UP)); f.setImpuestos(impuestos); f.setDescuentos(descuento); f.setTotal(total);
        f.setMotivoDescuento(rq.motivoDescuento()==null?null:rq.motivoDescuento().trim());
        if(descuento.signum()>0) f.setDescuentoAutorizadoPor(usuarioId);
        f.setEstado("PENDIENTE"); f.setEmitidaEn(LocalDateTime.now()); f=facturas.saveAndFlush(f);
        for(FacturaDetalle d:lineas){d.setFacturaId(f.getId());detalles.save(d);} return f;
    }

    private static FacturaDetalle linea(String tipo,String descripcion,Long medicamentoId,Long loteId,Long despachoId,BigDecimal cantidad,BigDecimal precio,BigDecimal iva){
        FacturaDetalle d=new FacturaDetalle();d.setTipo(tipo);d.setDescripcion(descripcion);d.setMedicamentoId(medicamentoId);d.setLoteId(loteId);d.setDespachoDetalleId(despachoId);d.setCantidad(cantidad);d.setPrecioUnitario(precio.setScale(2,RoundingMode.HALF_UP));d.setImpuestoPorcentaje(iva);
        d.setSubtotal(precio.multiply(cantidad).setScale(2,RoundingMode.HALF_UP));return d;
    }
    private String json(Object o){try{return mapper.writeValueAsString(o);}catch(Exception e){throw new IllegalStateException("No se pudo guardar la copia histórica de facturación.",e);}}
    private static BigDecimal money(BigDecimal n){return n.setScale(2,RoundingMode.HALF_UP);}

    @Override @Transactional public Factura pagar(Long id,FacturaPagoRq rq,Long usuarioId){
        if(rq==null||rq.monto()==null||rq.monto().signum()<=0||rq.medio()==null||rq.medio().isBlank()||rq.medio().length()>40)throw new BadRequestException("Monto positivo y medio de pago son obligatorios.");
        Factura f=facturas.findLockedById(id).orElseThrow(()->new BadRequestException("La factura no existe."));
        if("ANULADA".equals(f.getEstado()))throw new BadRequestException("No se reciben pagos de facturas anuladas.");
        BigDecimal pagado=pagos.totalByFactura(id); BigDecimal nuevo=money(rq.monto());
        if(pagado.add(nuevo).compareTo(f.getTotal())>0)throw new BadRequestException("El pago supera el saldo pendiente.");
        FacturaPago p=new FacturaPago();p.setFacturaId(id);p.setMonto(nuevo);p.setMedio(rq.medio().trim());p.setReferencia(rq.referencia());p.setRecibidoPor(usuarioId);p.setPagadoEn(LocalDateTime.now());pagos.save(p);
        f.setEstado(pagado.add(nuevo).compareTo(f.getTotal())==0?"PAGADA":"PARCIAL");return facturas.save(f);
    }
    @Override @Transactional public Factura anular(Long id,String motivo,Long usuarioId){
        if(motivo==null||motivo.isBlank()||motivo.length()>500)throw new BadRequestException("La anulación requiere un motivo de máximo 500 caracteres.");
        Factura f=facturas.findLockedById(id).orElseThrow(()->new BadRequestException("La factura no existe."));
        if("ANULADA".equals(f.getEstado()))throw new BadRequestException("La factura ya está anulada.");
        if(pagos.existsByFacturaId(id))throw new BadRequestException("No se puede anular: registre primero la devolución de los pagos por el procedimiento contable de la clínica.");
        f.setEstado("ANULADA");f.setMotivoAnulacion(motivo.trim());f.setAnuladaPor(usuarioId);f.setAnuladaEn(LocalDateTime.now());return facturas.save(f);
    }
    @Override public List<Factura> porCliente(Long id){return facturas.findByClienteIdOrderByEmitidaEnDesc(id);}
    @Override public List<Factura> porMascota(Integer id){return facturas.findByMascotaIdOrderByEmitidaEnDesc(id);}
    @Override public Object detalle(Long id){Factura f=facturas.findById(id).orElseThrow(()->new BadRequestException("La factura no existe."));return Map.of("factura",f,"detalles",detalles.findByFacturaIdOrderByIdAsc(id),"pagos",pagos.findByFacturaIdOrderByPagadoEnAsc(id));}
    @Override public byte[] pdf(Long id){
        Factura f=facturas.findById(id).orElseThrow(()->new BadRequestException("La factura no existe."));
        List<String> rows=new ArrayList<>(); rows.add("Factura: "+f.getNumero()); rows.add("Estado: "+f.getEstado());
        rows.add("Emitida: "+f.getEmitidaEn()); rows.add("Clínica: "+f.getClinicaSnapshot()); rows.add("Propietario: "+f.getPropietarioSnapshot());
        rows.add("Mascota: "+f.getMascotaSnapshot()); rows.add("Veterinario: "+f.getMedicoSnapshot()); rows.add("Detalle:");
        for(FacturaDetalle d:detalles.findByFacturaIdOrderByIdAsc(id)) rows.add(d.getDescripcion()+" | "+d.getCantidad()+" x "+d.getPrecioUnitario()+" = "+d.getSubtotal()+" (IVA "+d.getImpuestoPorcentaje()+"%)");
        rows.add("Subtotal: "+f.getSubtotal()); rows.add("Impuestos: "+f.getImpuestos()); rows.add("Descuentos: "+f.getDescuentos()); rows.add("Total: "+f.getTotal());
        rows.add("Pagos:"); for(FacturaPago p:pagos.findByFacturaIdOrderByPagadoEnAsc(id)) rows.add(p.getPagadoEn()+" | "+p.getMedio()+" | "+p.getMonto()+" | "+Objects.toString(p.getReferencia(),""));
        return com.uniminuto.clinica.util.PdfGenerator.crear("Factura de servicios veterinarios",rows);
    }
}
