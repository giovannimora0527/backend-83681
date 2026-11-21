package com.uniminuto.clinica.apicontroller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/factura/gestion")
public class FacturaGestionApiController {
    private final JdbcTemplate db;
    public FacturaGestionApiController(JdbcTemplate db) { this.db = db; }
    public record Detalle(String tipo, String descripcion, Long medicamentoId, Integer cantidad,
                          BigDecimal precioUnitario, BigDecimal porcentajeIva) {}
    public record Crear(Long citaId, List<Detalle> detalles, BigDecimal descuento) {}
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static BigDecimal dinero(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
    private static ResponseStatusException error(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,m); }

    @GetMapping("/{id}")
    public Map<String,Object> consultar(@PathVariable long id) {
        var filas=db.queryForList("SELECT * FROM factura WHERE id=?",id);
        if(filas.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Factura inexistente");
        return Map.of("factura",filas.get(0),"detalles",db.queryForList("SELECT * FROM factura_detalle WHERE factura_id=? ORDER BY id",id),
                "pagos",db.queryForList("SELECT id,valor,metodo,fecha FROM pago WHERE factura_id=? ORDER BY id",id));
    }

    @PostMapping
    @Transactional
    public Map<String,Object> crear(@RequestBody Crear rq) {
        if(rq==null || rq.citaId()==null || rq.detalles()==null || rq.detalles().isEmpty()) throw error("Cita y detalles requeridos");
        var citas=db.queryForList("SELECT cliente_id,mascota_id,medico_id,estado FROM cita WHERE id=? FOR UPDATE",rq.citaId());
        if(citas.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Cita inexistente");
        if("CANCELADA".equalsIgnoreCase(String.valueOf(citas.get(0).get("estado")))) throw error("Cita cancelada");
        if(!db.queryForList("SELECT id FROM factura WHERE cita_id=?",rq.citaId()).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT,"La cita ya tiene factura");
        BigDecimal subtotal=BigDecimal.ZERO, iva=BigDecimal.ZERO;
        List<BigDecimal> subtotales=new ArrayList<>();
        for(Detalle d:rq.detalles()) {
            if(d==null || d.descripcion()==null || d.descripcion().isBlank() || d.descripcion().length()>200 || d.tipo()==null ||
               !Set.of("SERVICIO","MEDICAMENTO","PRODUCTO").contains(d.tipo()) || d.cantidad()==null || d.cantidad()<=0 ||
               d.precioUnitario()==null || d.precioUnitario().signum()<0 || d.porcentajeIva()==null ||
               d.porcentajeIva().signum()<0 || d.porcentajeIva().compareTo(CIEN)>0) throw error("Detalle invalido");
            BigDecimal linea=dinero(d.precioUnitario().multiply(BigDecimal.valueOf(d.cantidad())));
            subtotal=subtotal.add(linea);
            iva=iva.add(dinero(linea.multiply(d.porcentajeIva()).divide(CIEN,2,RoundingMode.HALF_UP)));
            subtotales.add(linea);
        }
        BigDecimal descuento=rq.descuento()==null?BigDecimal.ZERO:dinero(rq.descuento());
        if(descuento.signum()<0 || descuento.compareTo(subtotal)>0) throw error("Descuento invalido");
        BigDecimal total=dinero(subtotal.subtract(descuento).add(iva));
        var c=citas.get(0);
        db.update("INSERT INTO factura(numero,cita_id,cliente_id,mascota_id,medico_id,fecha,subtotal,descuento,iva,total,estado) " +
                  "VALUES(?,?,?,?,?,NOW(),?,?,?,?,?)", "TEMP-"+UUID.randomUUID().toString().substring(0,15),rq.citaId(),
                  c.get("cliente_id"),c.get("mascota_id"),c.get("medico_id"),subtotal,descuento,iva,total,"PENDIENTE");
        Long id=db.queryForObject("SELECT id FROM factura WHERE cita_id=?",Long.class,rq.citaId());
        String numero="FAC-"+id;
        db.update("UPDATE factura SET numero=? WHERE id=?",numero,id);
        for(int i=0;i<rq.detalles().size();i++) {
            Detalle d=rq.detalles().get(i);
            db.update("INSERT INTO factura_detalle(factura_id,tipo,descripcion,medicamento_id,cantidad,precio_unitario,porcentaje_iva,subtotal) VALUES(?,?,?,?,?,?,?,?)",
                    id,d.tipo(),d.descripcion(),d.medicamentoId(),d.cantidad(),dinero(d.precioUnitario()),d.porcentajeIva(),subtotales.get(i));
        }
        return Map.of("id",id,"numero",numero,"subtotal",subtotal,"iva",iva,"descuento",descuento,"total",total,"estado","PENDIENTE");
    }
}
