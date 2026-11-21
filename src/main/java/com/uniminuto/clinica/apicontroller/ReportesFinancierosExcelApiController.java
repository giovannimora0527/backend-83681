package com.uniminuto.clinica.apicontroller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/reportes/financieros")
public class ReportesFinancierosExcelApiController {
    private final JdbcTemplate db;
    public ReportesFinancierosExcelApiController(JdbcTemplate db){this.db=db;}

    @GetMapping("/facturas.xlsx")
    public ResponseEntity<byte[]> facturas(@RequestParam LocalDate desde,@RequestParam LocalDate hasta) throws IOException {
        if(desde.isAfter(hasta)) return ResponseEntity.badRequest().build();
        List<Map<String,Object>> rows=db.queryForList(
            "SELECT f.numero,f.fecha,f.cita_id,f.cliente_id,f.estado,f.subtotal,f.descuento,f.iva,f.total,COALESCE(SUM(p.valor),0) AS pagado " +
            "FROM factura f LEFT JOIN pago p ON p.factura_id=f.id " +
            "WHERE f.fecha >= ? AND f.fecha < ? " +
            "GROUP BY f.id,f.numero,f.fecha,f.cita_id,f.cliente_id,f.estado,f.subtotal,f.descuento,f.iva,f.total ORDER BY f.fecha,f.id",
            java.sql.Timestamp.valueOf(desde.atStartOfDay()),java.sql.Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        String[] keys={"numero","fecha","cita_id","cliente_id","estado","subtotal","descuento","iva","total","pagado"};
        try(XSSFWorkbook wb=new XSSFWorkbook();ByteArrayOutputStream out=new ByteArrayOutputStream()){
            Sheet sheet=wb.createSheet("Facturas");
            Row header=sheet.createRow(0);
            for(int c=0;c<keys.length;c++)header.createCell(c).setCellValue(keys[c]);
            int r=1;
            for(Map<String,Object> item:rows){Row row=sheet.createRow(r++);
                for(int c=0;c<keys.length;c++){
                    Object value=item.get(keys[c]);Cell cell=row.createCell(c);
                    if(value instanceof Number)cell.setCellValue(((Number)value).doubleValue());
                    else cell.setCellValue(value==null?"":value.toString());
                }
            }
            for(int c=0;c<keys.length;c++)sheet.setColumnWidth(c,5500);
            wb.write(out);
            return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"facturas.xlsx\"").body(out.toByteArray());
        }
    }
}
