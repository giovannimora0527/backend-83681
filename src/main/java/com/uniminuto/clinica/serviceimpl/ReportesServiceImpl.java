package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.ReportesService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class ReportesServiceImpl implements ReportesService {
    private final MedicamentoRepository medicamentos; private final InventarioLoteRepository lotes;
    private final InventarioMovimientoRepository movimientos; private final CitaRepository citas;
    private final FacturaRepository facturas; private final FacturaDetalleRepository detalles;
    private final FacturaPagoRepository pagos; private final AtencionMedicaRepository atenciones;
    public ReportesServiceImpl(MedicamentoRepository medicamentos,InventarioLoteRepository lotes,InventarioMovimientoRepository movimientos,
            CitaRepository citas,FacturaRepository facturas,FacturaDetalleRepository detalles,FacturaPagoRepository pagos,AtencionMedicaRepository atenciones){
        this.medicamentos=medicamentos;this.lotes=lotes;this.movimientos=movimientos;this.citas=citas;this.facturas=facturas;this.detalles=detalles;this.pagos=pagos;this.atenciones=atenciones;
    }
    @Override @Transactional(readOnly=true) public Map<String,Object> inventario(){
        List<Map<String,Object>> rows=new ArrayList<>(); BigDecimal costo=BigDecimal.ZERO,venta=BigDecimal.ZERO; int bajo=0,agotados=0,vencidos=0,porVencer=0;
        Map<Long,List<InventarioLote>> porProducto=new HashMap<>();for(InventarioLote l:lotes.findAll())porProducto.computeIfAbsent(l.getMedicamento().getId(),x->new ArrayList<>()).add(l);
        LocalDate hoy=LocalDate.now(),limite=hoy.plusDays(30);
        for(Medicamento m:medicamentos.findAll()){
            List<InventarioLote> ls=porProducto.getOrDefault(m.getId(),List.of()); BigDecimal stock=ls.stream().map(InventarioLote::getCantidadDisponible).reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal valorCosto=ls.stream().map(l->l.getCantidadDisponible().multiply(l.getPrecioCompraUnitario())).reduce(BigDecimal.ZERO,BigDecimal::add);
            BigDecimal valorVenta=ls.stream().map(l->l.getCantidadDisponible().multiply(l.getPrecioVentaUnitario())).reduce(BigDecimal.ZERO,BigDecimal::add);
            boolean out=stock.signum()==0,low=!out&&stock.compareTo(m.getStockMinimo())<=0; long expired=ls.stream().filter(l->l.getCantidadDisponible().signum()>0&&l.getFechaVencimiento().isBefore(hoy)).count();
            long near=ls.stream().filter(l->l.getCantidadDisponible().signum()>0&&!l.getFechaVencimiento().isBefore(hoy)&&!l.getFechaVencimiento().isAfter(limite)).count();
            if(out)agotados++;if(low)bajo++;if(expired>0)vencidos++;if(near>0)porVencer++;costo=costo.add(valorCosto);venta=venta.add(valorVenta);
            rows.add(row("producto",m.getNombre(),"codigo",m.getCodigo(),"tipo",m.getTipoProducto(),"stock",stock,"minimo",m.getStockMinimo(),"precioCompraCatalogo",m.getPrecioCompra(),"precioVentaCatalogo",m.getPrecioVenta(),"valorCosto",valorCosto,"valorVenta",valorVenta,"agotado",out,"stockBajo",low,"lotesVencidos",expired,"lotesPorVencer30Dias",near));
        }
        List<Map<String,Object>> kardex=movimientos.findAll().stream().map(m->row("fecha",m.getCreadoEn(),"producto",m.getLote().getMedicamento().getNombre(),"lote",m.getLote().getNumeroLote(),"tipo",m.getTipo(),"cantidad",m.getCantidad(),"referencia",Objects.toString(m.getReferenciaTipo(),"")+" "+Objects.toString(m.getReferenciaId(),""),"responsableId",m.getUsuarioId(),"motivo",m.getMotivo())).toList();
        return row("generadoEn",LocalDateTime.now(),"totalProductos",rows.size(),"agotados",agotados,"stockBajo",bajo,"productosConLotesVencidos",vencidos,"productosConLotesPorVencer30Dias",porVencer,"valorCosto",costo,"valorPotencialVenta",venta,"filas",rows,"movimientos",kardex);
    }
    @Override @Transactional(readOnly=true) public Map<String,Object> citas(LocalDateTime desde,LocalDateTime hasta,Long medicoId){
        validar(desde,hasta); List<Cita> encontrados=citas.findByFechaHoraBetween(desde,hasta).stream().filter(c->medicoId==null||medicoId.equals(c.getMedico().getId())).toList();
        Map<String,Long> estados=new TreeMap<>(); encontrados.forEach(c->estados.merge(c.getEstado(),1L,Long::sum));
        List<Map<String,Object>> rows=encontrados.stream().map(c->row("id",c.getId(),"fechaInicio",c.getFechaHora(),"fechaFin",c.getFechaFin(),"estado",c.getEstado(),"motivo",c.getMotivo(),"mascota",c.getMascota().getNombreMascota(),"propietario",c.getCliente().getNombres()+" "+c.getCliente().getApellidos(),"medico",c.getMedico().getNombres()+" "+c.getMedico().getApellidos(),"especialidad",c.getMedico().getEspecializacion().getNombre())).toList();
        return Map.of("desde",desde,"hasta",hasta,"medicoId",medicoId==null?"TODOS":medicoId,"total",rows.size(),"estados",estados,"filas",rows,"generadoEn",LocalDateTime.now());
    }
    @Override @Transactional(readOnly=true) public Map<String,Object> financiero(LocalDateTime desde,LocalDateTime hasta){
        validar(desde,hasta); List<Factura> fs=facturas.findByEmitidaEnBetweenOrderByEmitidaEnAsc(desde,hasta); List<Factura> vigentes=fs.stream().filter(f->!"ANULADA".equals(f.getEstado())).toList();
        BigDecimal emitido=BigDecimal.ZERO,iva=BigDecimal.ZERO,descuento=BigDecimal.ZERO,servicios=BigDecimal.ZERO,medicamentosFacturados=BigDecimal.ZERO;
        Map<String,BigDecimal> porMedico=new TreeMap<>(),porEspecialidad=new TreeMap<>(); Map<String,Long> estados=new TreeMap<>(); List<Map<String,Object>> rows=new ArrayList<>();
        for(Factura f:vigentes){emitido=emitido.add(f.getTotal());iva=iva.add(f.getImpuestos());descuento=descuento.add(f.getDescuentos());estados.merge(f.getEstado(),1L,Long::sum);
            for(FacturaDetalle d:detalles.findByFacturaIdOrderByIdAsc(f.getId())){if("SERVICIO".equals(d.getTipo()))servicios=servicios.add(d.getSubtotal());else medicamentosFacturados=medicamentosFacturados.add(d.getSubtotal());}
            String medico="ID "+f.getMedicoId(); String especialidad="SIN ESPECIALIDAD";
            AtencionMedica atencion=atenciones.findById(f.getAtencionId()).orElse(null);
            if(atencion!=null){Cita cita=citas.findById(atencion.getCitaId()).orElse(null);if(cita!=null){medico=cita.getMedico().getNombres()+" "+cita.getMedico().getApellidos();especialidad=Objects.toString(cita.getMedico().getEspecializacion().getNombre(),especialidad);}}
            porMedico.merge(medico,f.getTotal(),BigDecimal::add);porEspecialidad.merge(especialidad,f.getTotal(),BigDecimal::add);
            rows.add(row("factura",f.getNumero(),"fecha",f.getEmitidaEn(),"estado",f.getEstado(),"medicoId",f.getMedicoId(),"subtotal",f.getSubtotal(),"impuestos",f.getImpuestos(),"descuentos",f.getDescuentos(),"total",f.getTotal()));
        }
        BigDecimal recaudado=BigDecimal.ZERO;List<Map<String,Object>> pagosFilas=new ArrayList<>();
        Map<String,BigDecimal> dia=new TreeMap<>(),mes=new TreeMap<>(),anio=new TreeMap<>(),recaudoMedico=new TreeMap<>(),recaudoEspecialidad=new TreeMap<>();
        for(FacturaPago p:pagos.findByPagadoEnBetweenOrderByPagadoEnAsc(desde,hasta)){
            recaudado=recaudado.add(p.getMonto());dia.merge(p.getPagadoEn().toLocalDate().toString(),p.getMonto(),BigDecimal::add);
            mes.merge(java.time.YearMonth.from(p.getPagadoEn()).toString(),p.getMonto(),BigDecimal::add);anio.merge(String.valueOf(p.getPagadoEn().getYear()),p.getMonto(),BigDecimal::add);
            Factura f=facturas.findById(p.getFacturaId()).orElse(null);
            if(f!=null){AtencionMedica a=atenciones.findById(f.getAtencionId()).orElse(null);Cita c=a==null?null:citas.findById(a.getCitaId()).orElse(null);
                String med=c==null?"ID "+f.getMedicoId():c.getMedico().getNombres()+" "+c.getMedico().getApellidos();String esp=c==null?"SIN ESPECIALIDAD":Objects.toString(c.getMedico().getEspecializacion().getNombre(),"SIN ESPECIALIDAD");
                recaudoMedico.merge(med,p.getMonto(),BigDecimal::add);recaudoEspecialidad.merge(esp,p.getMonto(),BigDecimal::add);}
            pagosFilas.add(row("fecha",p.getPagadoEn(),"facturaId",p.getFacturaId(),"medio",p.getMedio(),"monto",p.getMonto(),"referencia",Objects.toString(p.getReferencia(),"")));
        }
        return row("desde",desde,"hasta",hasta,"generadoEn",LocalDateTime.now(),"facturasEmitidas",fs.size(),"estados",estados,"totalFacturado",emitido,"ingresosPorPagos",recaudado,"serviciosFacturados",servicios,"medicamentosFacturados",medicamentosFacturados,"impuestos",iva,"descuentos",descuento,"facturadoPorVeterinario",porMedico,"facturadoPorEspecialidad",porEspecialidad,"recaudadoPorVeterinario",recaudoMedico,"recaudadoPorEspecialidad",recaudoEspecialidad,"recaudadoPorDia",dia,"recaudadoPorMes",mes,"recaudadoPorAnio",anio,"filas",rows,"pagos",pagosFilas);
    }
    @Override public byte[] excel(String tipo,LocalDateTime desde,LocalDateTime hasta,Long medicoId){
        Map<String,Object> data=datos(tipo,desde,hasta,medicoId);Object rowsObj=data.get("filas");List<?> rows=rowsObj instanceof List<?> l?l:List.of();
        try(Workbook wb=new XSSFWorkbook();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            Sheet summary=wb.createSheet("Resumen"); CellStyle header=wb.createCellStyle();Font font=wb.createFont();font.setBold(true);header.setFont(font);int summaryRow=0;
            for(var e:data.entrySet()) if(!(e.getValue() instanceof List)){Row row=summary.createRow(summaryRow++);Cell k=row.createCell(0);k.setCellValue(e.getKey());k.setCellStyle(header);row.createCell(1).setCellValue(Objects.toString(e.getValue(),""));}
            Sheet sheet=wb.createSheet("Detalle");
            if(rows.isEmpty()){sheet.createRow(0).createCell(0).setCellValue("Sin registros para los filtros indicados");}
            else {Map<?,?> first=(Map<?,?>)rows.get(0);List<?> columns=new ArrayList<>(first.keySet());Row hr=sheet.createRow(0);for(int i=0;i<columns.size();i++){Cell c=hr.createCell(i);c.setCellValue(String.valueOf(columns.get(i)));c.setCellStyle(header);}
                for(int r=0;r<rows.size();r++){Map<?,?> item=(Map<?,?>)rows.get(r);Row row=sheet.createRow(r+1);for(int c=0;c<columns.size();c++){Object v=item.get(columns.get(c));Cell cell=row.createCell(c);if(v instanceof Number n)cell.setCellValue(n.doubleValue());else if(v instanceof Boolean b)cell.setCellValue(b);else cell.setCellValue(Objects.toString(v,""));}}
                for(int c=0;c<columns.size();c++)sheet.autoSizeColumn(c);
            }
            Object movs=data.get("movimientos");if(movs instanceof List<?> list&&!list.isEmpty())escribirHoja(wb,"Movimientos",list,header);
            Object pagosFilas=data.get("pagos");if(pagosFilas instanceof List<?> list&&!list.isEmpty())escribirHoja(wb,"Pagos",list,header);
            wb.write(out);return out.toByteArray();
        }catch(Exception e){throw new IllegalStateException("No se pudo exportar el reporte Excel.",e);}
    }
    private static void escribirHoja(Workbook wb,String nombre,List<?> rows,CellStyle header){Sheet sheet=wb.createSheet(nombre);if(rows.isEmpty())return;Map<?,?> first=(Map<?,?>)rows.get(0);List<?> columns=new ArrayList<>(first.keySet());Row hr=sheet.createRow(0);for(int c=0;c<columns.size();c++){Cell cell=hr.createCell(c);cell.setCellValue(String.valueOf(columns.get(c)));cell.setCellStyle(header);}for(int r=0;r<rows.size();r++){Map<?,?> data=(Map<?,?>)rows.get(r);Row line=sheet.createRow(r+1);for(int c=0;c<columns.size();c++)line.createCell(c).setCellValue(Objects.toString(data.get(columns.get(c)),""));}for(int c=0;c<columns.size();c++)sheet.autoSizeColumn(c);}
    @Override public byte[] pdf(String tipo,LocalDateTime desde,LocalDateTime hasta,Long medicoId){Map<String,Object> data=datos(tipo,desde,hasta,medicoId);List<String> lines=new ArrayList<>();for(var e:data.entrySet())if(!"filas".equals(e.getKey())&&!(e.getValue() instanceof List))lines.add(e.getKey()+": "+e.getValue());Object o=data.get("filas");if(o instanceof List<?> l)for(Object row:l)lines.add(row.toString());return com.uniminuto.clinica.util.PdfGenerator.crear("Reporte administrativo: "+tipo,lines);}
    private Map<String,Object> datos(String tipo,LocalDateTime desde,LocalDateTime hasta,Long medicoId){return switch(tipo.toLowerCase(Locale.ROOT)){case "inventario"->inventario();case "citas","medico"->citas(desde,hasta,medicoId);case "financiero"->financiero(desde,hasta);default->throw new BadRequestException("Tipo de reporte no soportado.");};}
    private static void validar(LocalDateTime desde,LocalDateTime hasta){if(desde==null||hasta==null||desde.isAfter(hasta))throw new BadRequestException("Indique un rango de fechas válido (desde <= hasta).");}
    private static Map<String,Object> row(Object... pairs){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)m.put(String.valueOf(pairs[i]),pairs[i+1]);return m;}
}
