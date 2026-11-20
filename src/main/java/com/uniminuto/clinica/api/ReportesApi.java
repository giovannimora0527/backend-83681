package com.uniminuto.clinica.api;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
@RequestMapping("/reportes") @CrossOrigin(origins="*", allowedHeaders="*")
public interface ReportesApi {
    @GetMapping("/inventario") ResponseEntity<?> inventario();
    @GetMapping(value="/inventario.xlsx",produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ResponseEntity<byte[]> inventarioExcel();
    @GetMapping("/citas") ResponseEntity<?> citas(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,@RequestParam(required=false) Long medicoId);
    @GetMapping(value="/citas.xlsx",produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ResponseEntity<byte[]> citasExcel(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,@RequestParam(required=false) Long medicoId);
    @GetMapping("/financiero") ResponseEntity<?> financiero(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta);
    @GetMapping(value="/financiero.xlsx",produces="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ResponseEntity<byte[]> financieroExcel(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta);
    @GetMapping(value="/{tipo}.pdf",produces="application/pdf") ResponseEntity<byte[]> pdf(@PathVariable String tipo,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,@RequestParam(required=false) Long medicoId);
}
