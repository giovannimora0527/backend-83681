package com.uniminuto.clinica.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public final class PdfUtil {
    private PdfUtil() { }
    public static byte[] generar(String titulo, List<String> lineas) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            java.util.List<String> filas = new java.util.ArrayList<>();
            for (String line : lineas) {
                String safe = ascii(line == null ? "" : line);
                if (safe.isEmpty()) filas.add("");
                for (int i = 0; i < safe.length(); i += 95)
                    filas.add(safe.substring(i, Math.min(i + 95, safe.length())));
            }
            int paginas = Math.max(1, (filas.size() + 42) / 43);
            for (int p = 0; p < paginas; p++) {
                PDPage page = new PDPage(); doc.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                    if (p == 0) {
                        content.beginText(); content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                        content.newLineAtOffset(50, 750); content.showText(ascii(titulo)); content.endText();
                    }
                    int end = Math.min(filas.size(), (p + 1) * 43);
                    float y = p == 0 ? 715 : 750;
                    for (int index = p * 43; index < end; index++) {
                        content.beginText(); content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                        content.newLineAtOffset(50, y); content.showText(filas.get(index)); content.endText(); y -= 16;
                    }
                }
            }
            doc.save(out); return out.toByteArray();
        } catch (IOException ex) { throw new IllegalStateException("No fue posible generar el PDF.", ex); }
    }
    private static String ascii(String value) {
        return value.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
