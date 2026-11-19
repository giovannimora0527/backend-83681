package com.uniminuto.clinica.util;

import com.uniminuto.clinica.models.ReporteRs;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Genera documentos PDF y libros de Excel a partir de las filas de un reporte. */
public final class ExportarReporte {

    /** Prepara los datos necesarios para usar esta clase. */
    private ExportarReporte() {
    }

    private static final String MARCA = "Clínica Veterinaria Uniminuto";
    private static final String MONEDA = "\"$\" #,##0.00";
    private static final DateTimeFormatter FECHA_EXCEL = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Conserva un total o conteo que se mostrará en la cabecera del informe. */
    public record Indicador(String nombre, Number valor, boolean moneda) {}

    /** Genera un libro con cabecera institucional y tabla de Excel. */
    public static byte[] excel(ReporteRs reporte, String nombreHoja, String contexto,
                               List<Indicador> indicadores) throws IOException {
        try (var libro = new XSSFWorkbook(); var salida = new ByteArrayOutputStream()) {
            hojaExcel(libro, nombreHoja, reporte, contexto, indicadores);
            libro.write(salida);
            return salida.toByteArray();
        }
    }

    /** Describe los filtros de los reportes de gestión en la cabecera del Excel. */
    public static String contexto(java.time.LocalDate inicio, java.time.LocalDate fin, Long medicoId, Long especializacionId) {
        return "Período: " + inicio + " a " + fin
                + "  |  Veterinario: " + (medicoId == null ? "Todos" : medicoId)
                + "  |  Especialidad: " + (especializacionId == null ? "Todas" : especializacionId);
    }

    /** Agrega una hoja con la presentación común de los informes de la clínica. */
    public static void hojaExcel(XSSFWorkbook libro, String nombreHoja, ReporteRs reporte, String contexto,
                                 List<Indicador> indicadores) throws IOException {
        var hoja = libro.createSheet(nombreHoja);
        hoja.setDisplayGridlines(false);
        int columnas = reporte.columnas().size();
        int ultimaColumnaCabecera = Math.max(3, columnas - 1);
        var formatos = libro.createDataFormat();
        var moneda = libro.createCellStyle();
        moneda.setDataFormat(formatos.getFormat(MONEDA));
        var decimal = libro.createCellStyle();
        decimal.setDataFormat(formatos.getFormat("#,##0.00"));
        var fecha = libro.createCellStyle();
        fecha.setDataFormat(formatos.getFormat("dd/mm/yyyy"));
        var fechaHora = libro.createCellStyle();
        fechaHora.setDataFormat(formatos.getFormat("dd/mm/yyyy hh:mm"));

        var banda = estiloTexto(libro, IndexedColors.WHITE, IndexedColors.DARK_BLUE, 16);
        banda.setVerticalAlignment(VerticalAlignment.CENTER);
        hoja.createRow(0).setHeightInPoints(43);
        for (int columna = 0; columna <= ultimaColumnaCabecera; columna++) {
            hoja.getRow(0).createCell(columna).setCellStyle(banda);
        }
        hoja.getRow(0).getCell(1).setCellValue(MARCA + "  |  " + reporte.titulo());
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 1, ultimaColumnaCabecera));
        agregarLogo(libro, hoja);

        hoja.createRow(1).setHeightInPoints(21);
        hoja.getRow(1).createCell(0).setCellValue(contexto);
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, ultimaColumnaCabecera));
        var generacion = hoja.createRow(2);
        generacion.createCell(0).setCellValue("Generado el " + FECHA_EXCEL.format(reporte.generado())
                + " (Bogotá)");
        hoja.addMergedRegion(new CellRangeAddress(2, 2, 0, ultimaColumnaCabecera));

        var etiqueta = estiloTexto(libro, IndexedColors.DARK_BLUE, IndexedColors.PALE_BLUE, 10);
        var cifra = estiloTexto(libro, IndexedColors.DARK_BLUE, IndexedColors.PALE_BLUE, 17);
        var cifraMoneda = libro.createCellStyle();
        cifraMoneda.cloneStyleFrom(cifra);
        cifraMoneda.setDataFormat(moneda.getDataFormat());
        int proxima = 4;
        for (int i = 0; i < indicadores.size(); i += 2) {
            var rotulos = hoja.createRow(proxima);
            rotulos.setHeightInPoints(18);
            var valores = hoja.createRow(proxima + 1);
            valores.setHeightInPoints(25);
            for (int j = i; j < Math.min(i + 2, indicadores.size()); j++) {
                var indicador = indicadores.get(j);
                int columna = (j - i) * 2;
                var rotulo = rotulos.createCell(columna);
                rotulo.setCellValue(indicador.nombre());
                rotulo.setCellStyle(etiqueta);
                rotulos.createCell(columna + 1).setCellStyle(etiqueta);
                var valor = valores.createCell(columna);
                if (indicador.valor() != null) valor.setCellValue(indicador.valor().doubleValue());
                valor.setCellStyle(indicador.moneda() ? cifraMoneda : cifra);
                valores.createCell(columna + 1).setCellStyle(valor.getCellStyle());
                hoja.addMergedRegion(new CellRangeAddress(proxima, proxima, columna, columna + 1));
                hoja.addMergedRegion(new CellRangeAddress(proxima + 1, proxima + 1, columna, columna + 1));
            }
            proxima += 3;
        }

        int filaEncabezados = proxima + 1;
        hoja.createRow(proxima).createCell(0).setCellValue("Detalle del informe");
        var encabezado = hoja.createRow(filaEncabezados);
        encabezado.setHeightInPoints(27);
        var estiloEncabezado = estiloTexto(libro, IndexedColors.WHITE, IndexedColors.DARK_BLUE, 11);
        estiloEncabezado.setWrapText(true);
        estiloEncabezado.setVerticalAlignment(VerticalAlignment.CENTER);
        for (int columna = 0; columna < columnas; columna++) {
            var celda = encabezado.createCell(columna);
            celda.setCellValue(reporte.columnas().get(columna));
            celda.setCellStyle(estiloEncabezado);
        }

        var total = estiloTexto(libro, IndexedColors.DARK_BLUE, IndexedColors.PALE_BLUE, 11);
        var totalMoneda = libro.createCellStyle();
        totalMoneda.cloneStyleFrom(total);
        totalMoneda.setDataFormat(moneda.getDataFormat());
        int indice = filaEncabezados + 1;
        for (var datos : reporte.filas()) {
            var fila = hoja.createRow(indice++);
            boolean filaTotal = datos.contains("TOTAL");
            for (int columna = 0; columna < columnas; columna++) {
                Object valor = columna < datos.size() ? datos.get(columna) : null;
                String titulo = reporte.columnas().get(columna);
                var celda = fila.createCell(columna);
                boolean dinero = titulo.contains("(COP)") || List.of("Importe", "IVA", "Descuentos").contains(titulo)
                        || ("Totales".equals(nombreHoja) && valor instanceof BigDecimal);
                if (valor instanceof Number numero) {
                    celda.setCellValue(numero.doubleValue());
                    if (dinero) celda.setCellStyle(filaTotal ? totalMoneda : moneda);
                    else if (!filaTotal && valor instanceof BigDecimal) celda.setCellStyle(decimal);
                } else if (valor instanceof java.time.LocalDateTime momento) {
                    celda.setCellValue(momento);
                    celda.setCellStyle(fechaHora);
                } else if (valor instanceof java.time.LocalDate dia) {
                    celda.setCellValue(dia);
                    celda.setCellStyle(fecha);
                } else if (valor instanceof Boolean bandera) {
                    celda.setCellValue(bandera);
                } else if (valor != null) {
                    celda.setCellValue(valor.toString());
                }
                if (filaTotal && !dinero) celda.setCellStyle(total);
            }
        }

        if (columnas > 0) {
            int ultimaFila = Math.max(filaEncabezados + 1, indice - 1);
            var rango = new AreaReference(new CellReference(filaEncabezados, 0),
                    new CellReference(ultimaFila, columnas - 1), SpreadsheetVersion.EXCEL2007);
            var tabla = hoja.createTable(rango);
            String nombreTabla = "DatosReporte" + libro.getNumberOfSheets();
            tabla.setName(nombreTabla);
            tabla.setDisplayName(nombreTabla);
            tabla.setStyleName("TableStyleMedium2");
            tabla.getCTTable().getTableStyleInfo().setShowRowStripes(true);
            if (!tabla.getCTTable().isSetAutoFilter()) {
                tabla.getCTTable().addNewAutoFilter().setRef(rango.formatAsString());
            }
            if (reporte.filas().isEmpty()) {
                hoja.createRow(ultimaFila + 1).createCell(0).setCellValue("Sin registros para los filtros consultados.");
            }

            for (int columna = 0; columna <= ultimaColumnaCabecera; columna++) {
                int ancho = columna < columnas ? reporte.columnas().get(columna).length() + 5 : 18;
                for (var datos : reporte.filas()) {
                    if (columna < datos.size() && datos.get(columna) != null) {
                        ancho = Math.max(ancho, datos.get(columna).toString().length() + 3);
                    }
                }
                hoja.setColumnWidth(columna, Math.min(52, Math.max(18, ancho)) * 256);
            }
        }
        hoja.createFreezePane(0, filaEncabezados + 1);
    }

    /** Inserta el logo de la clínica dentro de la banda superior. */
    private static void agregarLogo(XSSFWorkbook libro, org.apache.poi.xssf.usermodel.XSSFSheet hoja) throws IOException {
        int indice = 0;
        if (libro.getAllPictures().isEmpty()) {
            try (var logo = ExportarReporte.class.getResourceAsStream("/pdf/logo-vet.jpg")) {
                if (logo == null) throw new IOException("No se encontró el logo de la clínica.");
                indice = libro.addPicture(logo.readAllBytes(), Workbook.PICTURE_TYPE_JPEG);
            }
        }
        int px = org.apache.poi.util.Units.EMU_PER_PIXEL;
        var posicion = new org.apache.poi.xssf.usermodel.XSSFClientAnchor(8 * px, 6 * px, 54 * px, 52 * px,
                0, 0, 0, 0);
        hoja.createDrawingPatriarch().createPicture(posicion, indice);
    }

    /** Crea un estilo con texto en negrita y un fondo sólido. */
    private static CellStyle estiloTexto(XSSFWorkbook libro, IndexedColors texto, IndexedColors fondo, int puntos) {
        var fuente = libro.createFont();
        fuente.setBold(true);
        fuente.setColor(texto.getIndex());
        fuente.setFontHeightInPoints((short) puntos);

        var estilo = libro.createCellStyle();
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(fondo.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    /** Genera un PDF paginado con los encabezados y las filas del reporte. */
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

    /** Divide el texto en líneas que caben en el ancho de la página. */
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

    /** Escribe una línea de texto en la página del PDF. */
    private static void escribir(PDPageContentStream contenido, String texto, PDType1Font fuente)
            throws IOException {
        contenido.showText(limpiar(texto, fuente));
        contenido.newLine();
    }

    /** Sustituye los caracteres que la fuente del PDF no puede representar. */
    static String limpiar(String texto, PDType1Font fuente) {
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
