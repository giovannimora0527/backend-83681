package com.uniminuto.clinica.support;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/** Utilidades para construir PDFs con OpenPDF (encabezado de la clínica, tablas, formato de dinero). */
public final class PdfKit {

    public static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    public static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    public static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    public static final Font BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    public static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, Color.GRAY);
    private static final Font HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE);
    private static final Color HEAD_BG = new Color(0x1F, 0x4E, 0x79);

    private PdfKit() {
    }

    /** Cuerpo de un PDF; puede lanzar la excepción checked de OpenPDF. */
    public interface Body {
        void build(Document doc) throws Exception;
    }

    public static byte[] render(boolean landscape, Body body) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(landscape ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();
            body.build(doc);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }

    /** Encabezado con los datos de la clínica (tabla clinica_config) y el título del documento. */
    public static void header(Document doc, Map<String, Object> clinica, String titulo) throws Exception {
        String nombre = clinica == null ? "Clínica Veterinaria" : String.valueOf(clinica.get("nombre"));
        Paragraph p = new Paragraph(nombre, TITLE);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
        if (clinica != null) {
            Paragraph d = new Paragraph("NIT " + clinica.get("nit") + "  |  " + nz(clinica.get("direccion"))
                    + "  |  Tel. " + nz(clinica.get("telefono")) + "  |  " + nz(clinica.get("correo")), SMALL);
            d.setAlignment(Element.ALIGN_CENTER);
            doc.add(d);
        }
        Paragraph t = new Paragraph(titulo, H2);
        t.setAlignment(Element.ALIGN_CENTER);
        t.setSpacingBefore(10);
        t.setSpacingAfter(8);
        doc.add(t);
    }

    public static void line(Document doc, String label, String value) throws Exception {
        Phrase ph = new Phrase();
        ph.add(new com.lowagie.text.Chunk(label + ": ", BOLD));
        ph.add(new com.lowagie.text.Chunk(value == null ? "" : value, NORMAL));
        doc.add(new Paragraph(ph));
    }

    public static void section(Document doc, String titulo) throws Exception {
        Paragraph p = new Paragraph(titulo, H2);
        p.setSpacingBefore(8);
        p.setSpacingAfter(3);
        doc.add(p);
    }

    public static PdfPTable table(float[] widths, String... headers) {
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, HEAD));
            c.setBackgroundColor(HEAD_BG);
            c.setPadding(4);
            t.addCell(c);
        }
        t.setHeaderRows(1);
        return t;
    }

    public static void cell(PdfPTable t, Object text, boolean right) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : String.valueOf(text), NORMAL));
        c.setPadding(3);
        if (right) {
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        t.addCell(c);
    }

    public static void cellBold(PdfPTable t, Object text, boolean right) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : String.valueOf(text), BOLD));
        c.setPadding(3);
        if (right) {
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        t.addCell(c);
    }

    public static String money(Object n) {
        BigDecimal v = n == null ? BigDecimal.ZERO : (n instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(n)));
        DecimalFormat df = new DecimalFormat("$ #,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(v);
    }

    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public static String nz(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
