package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/inventario/documentos")
public class InventarioDocumentosApiController {
    private final MedicamentoRepository medicamentos;
    public InventarioDocumentosApiController(MedicamentoRepository medicamentos) { this.medicamentos=medicamentos; }
    private static String limpio(String s) {return s == null ? "" : s.replaceAll("[^\\x20-\\x7E]", " ");}
    @GetMapping("/estado.xlsx")
    public ResponseEntity<byte[]> excel() throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Inventario");
            String[] titulos={"ID","Codigo","Nombre","Tipo","Stock","Stock minimo","Vencimiento","Precio compra","Precio venta"};
            Row encabezado=hoja.createRow(0);
            for(int i=0;i<titulos.length;i++)encabezado.createCell(i).setCellValue(titulos[i]);
            int fila=1;
            for(Medicamento m:medicamentos.findAll()) {
                Row r=hoja.createRow(fila++);
                r.createCell(0).setCellValue(m.getId()==null?0:m.getId());
                r.createCell(1).setCellValue(m.getCodigo()==null?"":m.getCodigo());
                r.createCell(2).setCellValue(m.getNombre()==null?"":m.getNombre());
                r.createCell(3).setCellValue(m.getTipoProducto()==null?"":m.getTipoProducto());
                r.createCell(4).setCellValue(m.getStockActual()==null?0:m.getStockActual());
                r.createCell(5).setCellValue(m.getStockMinimo()==null?0:m.getStockMinimo());
                r.createCell(6).setCellValue(m.getFechaVence()==null?"":m.getFechaVence().toString());
                r.createCell(7).setCellValue(m.getPrecioCompra()==null?0:m.getPrecioCompra().doubleValue());
                r.createCell(8).setCellValue(m.getPrecioVenta()==null?0:m.getPrecioVenta().doubleValue());
            }
            for(int i=0;i<titulos.length;i++)hoja.setColumnWidth(i, Math.min(9000, (titulos[i].length()+16)*256));
            libro.write(salida);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=inventario.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(salida.toByteArray());
        }
    }
    @GetMapping("/estado.pdf")
    public ResponseEntity<byte[]> pdf() throws IOException {
        List<Medicamento> datos=medicamentos.findAll();
        try(PDDocument documento=new PDDocument();ByteArrayOutputStream salida=new ByteArrayOutputStream()) {
            PDType1Font normal=new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font negrita=new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            int indice=0;
            do {
                PDPage pagina=new PDPage(PDRectangle.A4);documento.addPage(pagina);
                try(PDPageContentStream contenido=new PDPageContentStream(documento,pagina)) {
                    contenido.beginText();contenido.setFont(negrita,15);contenido.newLineAtOffset(42,790);
                    contenido.showText("Reporte de inventario");contenido.endText();
                    float y=760;
                    contenido.beginText();contenido.setFont(negrita,9);contenido.newLineAtOffset(42,y);
                    contenido.showText("CODIGO             PRODUCTO                              STOCK      MINIMO      VENCE");contenido.endText();
                    y-=18;
                    while(indice<datos.size() && y>55) {
                        Medicamento m=datos.get(indice++);
                        String nombre=limpio(m.getNombre());if(nombre.length()>31)nombre=nombre.substring(0,31);
                        String codigo=limpio(m.getCodigo());
                        String linea=String.format("%-13s %-34s %6d %8d %s",codigo,nombre,
                                m.getStockActual()==null?0:m.getStockActual(),m.getStockMinimo()==null?0:m.getStockMinimo(),
                                m.getFechaVence()==null?"":m.getFechaVence().toString());
                        contenido.beginText();contenido.setFont(normal,8);contenido.newLineAtOffset(42,y);
                        contenido.showText(linea);contenido.endText();y-=15;
                    }
                }
            } while(indice<datos.size());
            documento.save(salida);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=inventario.pdf")
                    .contentType(MediaType.APPLICATION_PDF).body(salida.toByteArray());
        }
    }
}
