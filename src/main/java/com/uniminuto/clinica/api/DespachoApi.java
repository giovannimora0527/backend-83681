package com.uniminuto.clinica.api;
import com.uniminuto.clinica.entity.Despacho;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.entity.DespachoDetalle;
import com.uniminuto.clinica.models.ConfirmarDespachoRq;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@RequestMapping("/despacho")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface DespachoApi {
    @GetMapping("/formulas/pendientes") ResponseEntity<?> pendientes(@RequestParam(required=false) Long formulaId,@RequestParam(required=false) Integer mascotaId,@RequestParam(required=false) String propietario,
            @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime desde,
            @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime hasta);
    @GetMapping("/historial") ResponseEntity<?> historial(@RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime desde,
            @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime hasta,@RequestParam(required=false) Integer mascotaId,@RequestParam(required=false) String propietario);
    @GetMapping("/{despachoId}/detalles") ResponseEntity<java.util.List<DespachoDetalle>> detalles(@PathVariable Long despachoId);
    @PostMapping("/confirmar") ResponseEntity<Despacho> confirmar(@RequestBody ConfirmarDespachoRq request, HttpServletRequest httpRequest);
    @PostMapping(value="/{despachoId}/revertir", consumes="application/json", produces="application/json")
    ResponseEntity<Despacho> revertir(@PathVariable Long despachoId, @RequestBody java.util.Map<String,String> request, HttpServletRequest httpRequest);
}
