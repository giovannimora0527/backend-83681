package com.uniminuto.clinica.util;

import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.util.List;

public final class PdfGenerator {
    private PdfGenerator() {}
    public static byte[] crear(String titulo, List<String> lineas) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document documento = new Document(); PdfWriter.getInstance(documento, output); documento.open();
            Font encabezado = new Font(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), 16, Font.BOLD);
            Font cuerpo = new Font(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), 10, Font.NORMAL);
            documento.add(new Paragraph(titulo, encabezado)); documento.add(new Paragraph("Generado: " + java.time.LocalDateTime.now(), cuerpo));
            documento.add(new Paragraph(" ", cuerpo));
            for (String linea : lineas) documento.add(new Paragraph(linea == null ? "" : linea, cuerpo));
            documento.close(); return output.toByteArray();
        } catch (Exception e) { throw new IllegalStateException("No se pudo generar el documento PDF.", e); }
    }
}
