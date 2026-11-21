package com.uniminuto.clinica.apicontroller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/pago/gestion")
public class PagoGestionApiController {
    private final JdbcTemplate db;
    public PagoGestionApiController(JdbcTemplate db) { this.db=db; }
    public record Registrar(Long facturaId, BigDecimal valor, String metodo) {}

    @GetMapping("/factura/{id}")
    public Map<String,Object> consultar(@PathVariable long id) {
        var f=db.queryForList("SELECT id,numero,total,estado FROM factura WHERE id=?",id);
        if(f.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Factura inexistente");
        BigDecimal pagado=db.queryForObject("SELECT COALESCE(SUM(valor),0) FROM pago WHERE factura_id=?",BigDecimal.class,id);
        BigDecimal total=(BigDecimal)f.get(0).get("total");
        return Map.of("factura",f.get(0),"pagado",pagado,"pendiente",total.subtract(pagado),
                "pagos",db.queryForList("SELECT id,valor,metodo,fecha,usuario_id FROM pago WHERE factura_id=? ORDER BY id",id));
    }

    @PostMapping
    @Transactional
    public Map<String,Object> registrar(@RequestBody Registrar rq, HttpServletRequest request) {
        if(rq==null || rq.facturaId()==null || rq.valor()==null || rq.valor().signum()<=0 ||
                rq.valor().scale()>2 || rq.metodo()==null || !Set.of("EFECTIVO","TARJETA","TRANSFERENCIA").contains(rq.metodo()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Datos de pago invalidos");
        Object usuario=request.getAttribute("usuarioId");
        if(!(usuario instanceof Long)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Usuario no autenticado");
        var f=db.queryForList("SELECT id,total,estado FROM factura WHERE id=? FOR UPDATE",rq.facturaId());
        if(f.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Factura inexistente");
        if("ANULADA".equalsIgnoreCase(String.valueOf(f.get(0).get("estado"))))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Factura anulada");
        BigDecimal total=(BigDecimal)f.get(0).get("total");
        BigDecimal pagado=db.queryForObject("SELECT COALESCE(SUM(valor),0) FROM pago WHERE factura_id=?",BigDecimal.class,rq.facturaId());
        BigDecimal pendiente=total.subtract(pagado);
        if(rq.valor().compareTo(pendiente)>0) throw new ResponseStatusException(HttpStatus.CONFLICT,"El pago excede el saldo pendiente");
        db.update("INSERT INTO pago(factura_id,valor,metodo,fecha,usuario_id) VALUES(?,?,?,NOW(),?)",rq.facturaId(),rq.valor(),rq.metodo(),usuario);
        BigDecimal restante=pendiente.subtract(rq.valor());
        db.update("UPDATE factura SET estado=? WHERE id=?",restante.signum()==0?"PAGADA":"PENDIENTE",rq.facturaId());
        return Map.of("facturaId",rq.facturaId(),"pagado",rq.valor(),"pendiente",restante,"estado",restante.signum()==0?"PAGADA":"PENDIENTE");
    }
}
