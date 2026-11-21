package com.uniminuto.clinica.util;

import com.uniminuto.clinica.models.ReporteRs;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Presenta los reportes de la clínica como tablas paginadas. */
public final class InformePdf {

    private static final Color AZUL = new Color(23, 55, 87);
    private static final Color TINTA = new Color(34, 52, 66);
    private static final Color CLARO = new Color(239, 245, 250);
    private static final Color LINEA = new Color(211, 224, 234);
    private static final float MARGEN = 30;
    private static final float ANCHO = 782;
    private static final float PIE = 44;
    private static final float INTERLINEADO = 11;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private InformePdf() {
    }

    /** Genera un PDF a partir de un solo reporte. */
    public static byte[] generar(ReporteRs reporte, String contexto) throws IOException {
        return generar(reporte.titulo(), reporte.generado(), contexto, List.of(reporte));
    }

    /** Genera un libro de reportes con una tabla independiente por sección. */
    public static byte[] generar(String titulo, LocalDateTime generado, String contexto, List<ReporteRs> secciones)
            throws IOException {
        try (var pdf = new PDDocument(); var salida = new ByteArrayOutputStream();
             var recurso = InformePdf.class.getResourceAsStream("/pdf/logo-vet.jpg")) {
            var logo = recurso == null ? null : PDImageXObject.createFromByteArray(pdf, recurso.readAllBytes(), "Logo");
            for (var seccion : secciones) {
                escribirSeccion(pdf, logo, titulo, generado, contexto, seccion);
            }
            pdf.save(salida);
            return salida.toByteArray();
        }
    }

    /** Repite la cabecera y los encabezados cuando una tabla continúa en otra página. */
    private static void escribirSeccion(PDDocument pdf, PDImageXObject logo, String titulo,
                                        LocalDateTime generado, String contexto, ReporteRs seccion) throws IOException {
        var normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        var negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float[] anchos = anchos(seccion);
        var encabezados = envolver(seccion.columnas(), anchos, negrita, 9);
        int indice = 0;
        int desde = 0;
        List<List<String>> fila = null;
        do {
            var pagina = new PDPage(new PDRectangle(842, 595));
            pdf.addPage(pagina);
            try (var lienzo = new PDPageContentStream(pdf, pagina)) {
                rectangulo(lienzo, MARGEN, 514, ANCHO, 51, AZUL);
                if (logo != null) lienzo.drawImage(logo, 41, 522, 35, 35);
                texto(lienzo, negrita, 9, Color.WHITE, 86, 548, "CLÍNICA VETERINARIA UNIMINUTO");
                texto(lienzo, negrita, 16, Color.WHITE, 86, 528, titulo);
                float yContexto = 497;
                for (String dato : contexto.lines().toList()) {
                    for (String linea : envolver(List.of(dato), new float[]{ANCHO}, normal, 9).getFirst()) {
                        texto(lienzo, normal, 9, TINTA, MARGEN, yContexto, linea);
                        yContexto -= 14;
                    }
                }
                texto(lienzo, normal, 9, TINTA, MARGEN, yContexto, "Generado el " + FECHA.format(generado) + " (Bogotá)");
                texto(lienzo, negrita, 11, AZUL, MARGEN, yContexto - 22,
                        titulo.equals(seccion.titulo()) ? "Resultados del informe" : seccion.titulo());
                float y = dibujarFila(lienzo, encabezados, anchos, negrita, 9, Color.WHITE, AZUL,
                        yContexto - 33, 0, alto(encabezados), null);
                if (seccion.filas().isEmpty()) {
                    texto(lienzo, normal, 10, TINTA, MARGEN + 5, y - 20, "Sin registros para los filtros consultados.");
                }
                while (indice < seccion.filas().size()) {
                    var datos = seccion.filas().get(indice);
                    boolean total = datos.contains("TOTAL") || "Totales".equals(seccion.titulo());
                    var fuente = total ? negrita : normal;
                    if (fila == null) fila = envolver(datos, anchos, fuente, 9);
                    int restantes = alto(fila) - desde;
                    int capacidad = (int) ((y - PIE - 8) / INTERLINEADO);
                    if (capacidad <= 0 || restantes > capacidad && y < 390 && desde == 0) break;
                    int lineas = Math.min(restantes, capacidad);
                    Color fondo = total ? LINEA : indice % 2 == 0 ? Color.WHITE : CLARO;
                    y = dibujarFila(lienzo, fila, anchos, fuente, 9, TINTA, fondo, y, desde, lineas, datos);
                    desde += lineas;
                    if (desde == alto(fila)) {
                        indice++;
                        desde = 0;
                        fila = null;
                    }
                }
                rectangulo(lienzo, MARGEN, 35, ANCHO, 1, LINEA);
                texto(lienzo, normal, 9, TINTA, MARGEN, 22, "Clínica Veterinaria Uniminuto");
                texto(lienzo, normal, 9, TINTA, 744, 22, "Página " + pdf.getNumberOfPages());
            }
        } while (indice < seccion.filas().size());
    }

    /** Da espacio adicional a las columnas con nombres, descripciones o valores largos. */
    private static float[] anchos(ReporteRs seccion) {
        float[] anchos = new float[seccion.columnas().size()];
        float suma = 0;
        for (int col = 0; col < anchos.length; col++) {
            var nombre = seccion.columnas().get(col);
            int longitud = nombre.length() + 3;
            for (var fila : seccion.filas()) {
                if (col < fila.size()) longitud = Math.max(longitud, valor(fila.get(col)).length());
            }
            anchos[col] = Math.max(10, Math.min(32, longitud));
            suma += anchos[col];
        }
        for (int col = 0; col < anchos.length; col++) anchos[col] *= ANCHO / suma;
        return anchos;
    }

    /** Ajusta cada celda al ancho de su columna, incluso si contiene una palabra larga. */
    private static List<List<String>> envolver(List<?> datos, float[] anchos, PDType1Font fuente, int puntos)
            throws IOException {
        var celdas = new ArrayList<List<String>>();
        for (int col = 0; col < anchos.length; col++) {
            String resto = col < datos.size() ? ExportarReporte.limpiar(valor(datos.get(col)), fuente) : "";
            resto = resto.replaceAll("\\s+", " ").strip();
            var lineas = new ArrayList<String>();
            float ancho = anchos[col] - 10;
            while (medir(fuente, resto, puntos) > ancho) {
                int corte = 1;
                while (corte < resto.length() && medir(fuente, resto.substring(0, corte + 1), puntos) <= ancho) {
                    corte++;
                }
                int espacio = resto.lastIndexOf(' ', corte);
                if (espacio > 0) corte = espacio;
                lineas.add(resto.substring(0, corte));
                resto = resto.substring(corte).stripLeading();
            }
            lineas.add(resto);
            celdas.add(lineas);
        }
        return celdas;
    }

    /** Devuelve el número de líneas de la celda más alta. */
    private static int alto(List<List<String>> fila) {
        return fila.stream().mapToInt(List::size).max().orElse(1);
    }

    /** Dibuja una fila o el fragmento que cabe en la página actual. */
    private static float dibujarFila(PDPageContentStream lienzo, List<List<String>> fila, float[] anchos,
                                     PDType1Font fuente, int puntos, Color tinta, Color fondo, float y,
                                     int desde, int lineas, List<?> datos) throws IOException {
        float base = y - lineas * INTERLINEADO - 8;
        rectangulo(lienzo, MARGEN, base, ANCHO, y - base, fondo);
        float x = MARGEN;
        for (int col = 0; col < anchos.length; col++) {
            boolean numerica = datos != null && col < datos.size() && datos.get(col) instanceof Number;
            for (int i = 0; i < lineas && desde + i < fila.get(col).size(); i++) {
                String linea = fila.get(col).get(desde + i);
                float posicion = numerica ? x + anchos[col] - 5 - medir(fuente, linea, puntos) : x + 5;
                texto(lienzo, fuente, puntos, tinta, posicion, y - 12 - i * INTERLINEADO, linea);
            }
            x += anchos[col];
        }
        rectangulo(lienzo, MARGEN, base, ANCHO, .45f, LINEA);
        return base;
    }

    /** Presenta las fechas de forma legible y mantiene el contenido de cada dato. */
    private static String valor(Object dato) {
        if (dato instanceof LocalDateTime fecha) return FECHA.format(fecha);
        if (dato instanceof java.math.BigDecimal importe) {
            return java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("es-CO")).format(importe);
        }
        return dato == null ? "" : dato.toString();
    }

    /** Mide un texto en puntos para ajustarlo al ancho disponible. */
    private static float medir(PDType1Font fuente, String texto, int puntos) throws IOException {
        return fuente.getStringWidth(texto) * puntos / 1000;
    }

    /** Escribe texto en una posición de la página. */
    private static void texto(PDPageContentStream lienzo, PDType1Font fuente, int puntos, Color color,
                              float x, float y, String texto) throws IOException {
        lienzo.setNonStrokingColor(color);
        lienzo.beginText();
        lienzo.setFont(fuente, puntos);
        lienzo.newLineAtOffset(x, y);
        lienzo.showText(ExportarReporte.limpiar(texto, fuente));
        lienzo.endText();
    }

    /** Pinta un fondo o un separador. */
    private static void rectangulo(PDPageContentStream lienzo, float x, float y, float ancho, float alto,
                                   Color color) throws IOException {
        lienzo.setNonStrokingColor(color);
        lienzo.addRect(x, y, ancho, alto);
        lienzo.fill();
    }
}
