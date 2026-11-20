package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.api.ReportesApi;
import com.uniminuto.clinica.service.ReportesService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.Map;
@RestController
public class ReportesApiController implements ReportesApi {
    private final ReportesService service;
    public ReportesApiController(ReportesService service){this.service=service;}
    @Override public ResponseEntity<?> inventario(){return ResponseEntity.ok(service.inventario());}
    @Override public ResponseEntity<byte[]> inventarioExcel(){return excel("inventario",null,null,null);}
    @Override public ResponseEntity<?> citas(LocalDateTime desde,LocalDateTime hasta,Long medicoId){return ResponseEntity.ok(service.citas(desde,hasta,medicoId));}
    @Override public ResponseEntity<byte[]> citasExcel(LocalDateTime desde,LocalDateTime hasta,Long medicoId){return excel("citas",desde,hasta,medicoId);}
    @Override public ResponseEntity<?> financiero(LocalDateTime desde,LocalDateTime hasta){return ResponseEntity.ok(service.financiero(desde,hasta));}
    @Override public ResponseEntity<byte[]> financieroExcel(LocalDateTime desde,LocalDateTime hasta){return excel("financiero",desde,hasta,null);}
    @Override public ResponseEntity<byte[]> pdf(String tipo,LocalDateTime desde,LocalDateTime hasta,Long medicoId){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=reporte-"+tipo+".pdf").contentType(MediaType.APPLICATION_PDF).body(service.pdf(tipo,desde,hasta,medicoId));}
    private ResponseEntity<byte[]> excel(String tipo,LocalDateTime desde,LocalDateTime hasta,Long medicoId){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=reporte-"+tipo+".xlsx").contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(service.excel(tipo,desde,hasta,medicoId));}
}
