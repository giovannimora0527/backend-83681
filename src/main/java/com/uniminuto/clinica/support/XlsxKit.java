package com.uniminuto.clinica.support;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

/** Utilidades para generar archivos .xlsx con Apache POI. */
public final class XlsxKit {

    private XlsxKit() {
    }

    /**
     * Hoja con título, línea de información (fecha de generación, filtros), encabezados, filas y totales.
     *
     * @param titulo    título del reporte (primera fila)
     * @param info      texto secundario (puede ser null)
     * @param headers   encabezados de columna
     * @param rows      filas (cada celda: String, Number o null)
     * @param totalRow  fila de totales (puede ser null)
     */
    public static byte[] sheet(String sheetName, String titulo, String info, List<String> headers,
                               List<List<Object>> rows, List<Object> totalRow) {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet(safeName(sheetName));
            Font bold = wb.createFont();
            bold.setBold(true);
            CellStyle head = wb.createCellStyle();
            head.setFont(bold);
            head.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle boldStyle = wb.createCellStyle();
            boldStyle.setFont(bold);

            int r = 0;
            if (titulo != null) {
                Cell c = sh.createRow(r++).createCell(0);
                c.setCellValue(titulo);
                c.setCellStyle(boldStyle);
            }
            if (info != null) {
                sh.createRow(r++).createCell(0).setCellValue(info);
            }
            if (titulo != null || info != null) {
                r++;
            }
            Row hr = sh.createRow(r++);
            for (int i = 0; i < headers.size(); i++) {
                Cell c = hr.createCell(i);
                c.setCellValue(headers.get(i));
                c.setCellStyle(head);
            }
            for (List<Object> row : rows) {
                fill(sh.createRow(r++), row, null);
            }
            if (totalRow != null) {
                fill(sh.createRow(r), totalRow, boldStyle);
            }
            for (int i = 0; i < headers.size(); i++) {
                sh.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel: " + e.getMessage(), e);
        }
    }

    private static void fill(Row row, List<Object> values, CellStyle style) {
        for (int i = 0; i < values.size(); i++) {
            Cell c = row.createCell(i);
            Object v = values.get(i);
            if (v == null) {
                c.setBlank();
            } else if (v instanceof BigDecimal b) {
                c.setCellValue(b.doubleValue());
            } else if (v instanceof Number n) {
                c.setCellValue(n.doubleValue());
            } else if (v instanceof Boolean b) {
                c.setCellValue(b);
            } else {
                c.setCellValue(String.valueOf(v));
            }
            if (style != null) {
                c.setCellStyle(style);
            }
        }
    }

    private static String safeName(String n) {
        String s = n == null ? "Hoja1" : n.replaceAll("[\\\\/?*\\[\\]:]", " ");
        return s.length() > 31 ? s.substring(0, 31) : s;
    }
}
