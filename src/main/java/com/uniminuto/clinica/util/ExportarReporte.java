package com.uniminuto.clinica.util;

import com.uniminuto.clinica.models.ReporteRs;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ExportarReporte {

    private ExportarReporte() {
    }

    public static byte[] excel(ReporteRs reporte) throws IOException {
        try (var libro = new XSSFWorkbook(); var salida = new ByteArrayOutputStream()) {
            var hoja = libro.createSheet("Reporte");
            hoja.createRow(0).createCell(0).setCellValue(reporte.titulo());
            hoja.createRow(1).createCell(0).setCellValue("Generado: " + reporte.generado());
            var encabezado = hoja.createRow(2);
            for (int columna = 0; columna < reporte.columnas().size(); columna++) {
                encabezado.createCell(columna).setCellValue(reporte.columnas().get(columna));
                hoja.setColumnWidth(columna, 4400);
            }
            int indice = 3;
            for (var datos : reporte.filas()) {
                var fila = hoja.createRow(indice++);
                for (int columna = 0; columna < datos.size(); columna++) {
                    Object valor = datos.get(columna);
                    if (valor instanceof Number numero) {
                        fila.createCell(columna).setCellValue(numero.doubleValue());
                    } else {
                        fila.createCell(columna).setCellValue(String.valueOf(valor));
                    }
                }
            }
            hoja.createFreezePane(0, 3);
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    public static byte[] pdf(ReporteRs reporte) throws IOException {
        var fuente = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        var lineas = new ArrayList<String>();
        for (var fila : reporte.filas()) {
            String linea = fila.stream().map(String::valueOf).collect(Collectors.joining(" | "));
            lineas.addAll(ajustar(linea, fuente));
        }
        try (var documento = new PDDocument(); var salida = new ByteArrayOutputStream()) {
            for (int inicio = 0; inicio < Math.max(1, lineas.size()); inicio += 31) {
                var pagina = new PDPage(new PDRectangle(842, 595));
                documento.addPage(pagina);
                try (var contenido = new PDPageContentStream(documento, pagina)) {
                    contenido.beginText();
                    contenido.setFont(fuente, 10);
                    contenido.setLeading(15);
                    contenido.newLineAtOffset(35, 555);
                    escribir(contenido, reporte.titulo(), fuente);
                    escribir(contenido, "Generado: " + reporte.generado(), fuente);
                    escribir(contenido, String.join(" | ", reporte.columnas()), fuente);
                    for (int i = inicio; i < Math.min(inicio + 31, lineas.size()); i++) {
                        escribir(contenido, lineas.get(i), fuente);
                    }
                    contenido.endText();
                }
            }
            documento.save(salida);
            return salida.toByteArray();
        }
    }

    private static List<String> ajustar(String texto, PDType1Font fuente) throws IOException {
        var lineas = new ArrayList<String>();
        var linea = new StringBuilder();
        for (String palabra : texto.split("\\s+")) {
            if (!linea.isEmpty() && fuente.getStringWidth(limpiar(linea + " " + palabra, fuente)) > 77000) {
                lineas.add(linea.toString());
                linea.setLength(0);
            }
            if (!linea.isEmpty()) {
                linea.append(' ');
            }
            linea.append(palabra);
        }
        lineas.add(linea.toString());
        return lineas;
    }

    private static void escribir(PDPageContentStream contenido, String texto, PDType1Font fuente)
            throws IOException {
        contenido.showText(limpiar(texto, fuente));
        contenido.newLine();
    }

    private static String limpiar(String texto, PDType1Font fuente) {
        var limpio = new StringBuilder();
        for (int codigo : texto.codePoints().toArray()) {
            String caracter = new String(Character.toChars(codigo));
            try {
                fuente.encode(caracter);
                limpio.append(caracter);
            } catch (IllegalArgumentException | IOException ex) {
                limpio.append('?');
            }
        }
        return limpio.toString();
    }
}
