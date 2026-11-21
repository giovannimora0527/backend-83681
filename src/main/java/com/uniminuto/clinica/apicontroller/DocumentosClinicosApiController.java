package com.uniminuto.clinica.apicontroller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/reportes/documentos")
public class DocumentosClinicosApiController {
    private final JdbcTemplate db;
    public DocumentosClinicosApiController(JdbcTemplate db) { this.db = db; }

    @GetMapping("/factura/{id}.pdf")
    public ResponseEntity<byte[]> factura(@PathVariable long id) throws IOException {
        List<Map<String,Object>> cab = db.queryForList("SELECT numero, fecha, subtotal, descuento, iva, total, estado, cita_id, cliente_id, mascota_id FROM factura WHERE id=?", id);
        if (cab.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada");
        Map<String,Object> f=cab.get(0);
        List<String> lineas = new ArrayList<>();
        lineas.add("Factura: " + f.get("numero") + "  Fecha: " + f.get("fecha"));
        lineas.add("Cita: " + f.get("cita_id") + "  Cliente: " + f.get("cliente_id") + "  Mascota: " + f.get("mascota_id"));
        lineas.add("Estado: " + f.get("estado"));
        lineas.add("DETALLES");
        for(Map<String,Object> d:db.queryForList("SELECT descripcion, cantidad, precio_unitario, subtotal FROM factura_detalle WHERE factura_id=? ORDER BY id",id))
            lineas.add(d.get("descripcion")+" | Cant: "+d.get("cantidad")+" | Unit: "+d.get("precio_unitario")+" | Subtotal: "+d.get("subtotal"));
        lineas.add("Subtotal: "+f.get("subtotal")+"  Descuento: "+f.get("descuento")+"  IVA: "+f.get("iva"));
        lineas.add("TOTAL: "+f.get("total"));
        return pdf("Factura veterinaria",lineas,"factura-"+id+".pdf");
    }

    @GetMapping("/formula/{id}.pdf")
    public ResponseEntity<byte[]> formula(@PathVariable long id) throws IOException {
        List<Map<String,Object>> cab = db.queryForList("SELECT id,cita_id,medico_id,mascota_id,fecha,estado_despacho FROM formula WHERE id=?",id);
        if(cab.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Formula no encontrada");
        Map<String,Object> f=cab.get(0);
        List<String> lineas=new ArrayList<>();
        lineas.add("Formula #"+id+"  Fecha: "+f.get("fecha"));
        lineas.add("Cita: "+f.get("cita_id")+"  Medico: "+f.get("medico_id")+"  Mascota: "+f.get("mascota_id"));
        lineas.add("Estado despacho: "+f.get("estado_despacho"));
        lineas.add("MEDICAMENTOS E INDICACIONES");
        for(Map<String,Object> d:db.queryForList("SELECT m.nombre, fm.cantidad_prescrita, fm.dosis, fm.frecuencia, fm.duracion, fm.via_administracion, fm.indicaciones FROM formula_medica fm JOIN medicamento m ON m.id=fm.medicamento_id WHERE fm.formula_id=? ORDER BY fm.id",id)) {
            lineas.add(d.get("nombre")+" | Cantidad: "+d.get("cantidad_prescrita")+" | Dosis: "+d.get("dosis"));
            lineas.add("Frecuencia: "+d.get("frecuencia")+"  Duracion: "+d.get("duracion")+"  Via: "+d.get("via_administracion"));
            lineas.add("Indicaciones: "+d.get("indicaciones"));
        }
        return pdf("Formula medica veterinaria",lineas,"formula-"+id+".pdf");
    }

    private static String seguro(Object value) {
        String s=String.valueOf(value==null?"":value).replaceAll("[^\\x20-\\x7E]"," ");
        return s.length()>108?s.substring(0,108):s;
    }
    private ResponseEntity<byte[]> pdf(String titulo,List<String> lineas,String nombre) throws IOException {
        try(PDDocument doc=new PDDocument(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            PDType1Font normal=new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font bold=new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            int i=0;
            do {
                PDPage page=new PDPage(PDRectangle.A4);doc.addPage(page);
                try(PDPageContentStream cs=new PDPageContentStream(doc,page)) {
                    cs.beginText();cs.setFont(bold,15);cs.newLineAtOffset(42,790);cs.showText(seguro(titulo));cs.endText();
                    float y=755;
                    while(i<lineas.size()&&y>50){
                        cs.beginText();cs.setFont(normal,9);cs.newLineAtOffset(42,y);cs.showText(seguro(lineas.get(i++)));cs.endText();y-=17;
                    }
                }
            } while(i<lineas.size());
            doc.save(out);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\""+nombre+"\"").body(out.toByteArray());
        }
    }
}
