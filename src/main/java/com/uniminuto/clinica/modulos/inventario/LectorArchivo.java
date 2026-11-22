package com.uniminuto.clinica.modulos.inventario;

import com.uniminuto.clinica.exception.BadRequestException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Lee archivos .xlsx o .csv y los entrega como filas de texto con encabezados normalizados. */
public final class LectorArchivo {

    public static final int MAX_FILAS = 5000;

    /** Una fila de datos: número de fila del archivo (1 = encabezado) y valores por encabezado. */
    public record Fila(int numero, Map<String, String> valores) {
    }

    public record Tabla(List<String> encabezados, List<Fila> filas) {
    }

    private LectorArchivo() {
    }

    public static Tabla leer(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("Debe adjuntar un archivo .xlsx o .csv con datos.");
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            byte[] bytes = archivo.getBytes();
            if (nombre.endsWith(".xlsx")) {
                return leerXlsx(bytes);
            }
            if (nombre.endsWith(".csv")) {
                return leerCsv(bytes);
            }
        } catch (IOException e) {
            throw new BadRequestException("No fue posible leer el archivo: " + e.getMessage());
        }
        throw new BadRequestException("Formato no soportado. Use un archivo Excel (.xlsx) o CSV (.csv).");
    }

    // ------------------------------------------------------------------ XLSX

    private static Tabla leerXlsx(byte[] bytes) throws IOException {
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sh = wb.getSheetAt(0);
            FormulaEvaluator ev = wb.getCreationHelper().createFormulaEvaluator();
            List<List<String>> matriz = new ArrayList<>();
            List<Integer> numeros = new ArrayList<>();
            int ultima = sh.getLastRowNum();
            for (int r = 0; r <= ultima; r++) {
                Row row = sh.getRow(r);
                List<String> celdas = new ArrayList<>();
                int max = row == null ? 0 : Math.max(row.getLastCellNum(), 0);
                for (int c = 0; c < max; c++) {
                    celdas.add(row.getCell(c) == null ? "" : textoCelda(row.getCell(c), ev));
                }
                matriz.add(celdas);
                numeros.add(r + 1);
            }
            return construir(matriz, numeros);
        } catch (org.apache.poi.EmptyFileException | org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException e) {
            throw new BadRequestException("El archivo no es un Excel .xlsx válido.");
        }
    }

    private static String textoCelda(Cell cell, FormulaEvaluator ev) {
        CellType t = cell.getCellType();
        if (t == CellType.FORMULA) {
            t = ev.evaluateFormulaCell(cell);
        }
        switch (t) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                BigDecimal bd = BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros();
                return bd.toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    // ------------------------------------------------------------------ CSV

    private static Tabla leerCsv(byte[] bytes) {
        String texto = decodificar(bytes);
        if (texto.startsWith("﻿")) {
            texto = texto.substring(1);
        }
        int salto = texto.indexOf('\n');
        String primera = salto < 0 ? texto : texto.substring(0, salto);
        char delim = cuenta(primera, ';') > cuenta(primera, ',') ? ';' : ',';

        List<List<String>> matriz = new ArrayList<>();
        List<Integer> numeros = new ArrayList<>();
        List<String> actual = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean comillas = false;
        int linea = 1;
        int lineaInicio = 1;
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            if (comillas) {
                if (ch == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        campo.append('"');
                        i++;
                    } else {
                        comillas = false;
                    }
                } else {
                    if (ch == '\n') {
                        linea++;
                    }
                    campo.append(ch);
                }
            } else if (ch == '"') {
                comillas = true;
            } else if (ch == delim) {
                actual.add(campo.toString().trim());
                campo.setLength(0);
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') {
                    i++;
                }
                actual.add(campo.toString().trim());
                campo.setLength(0);
                matriz.add(actual);
                numeros.add(lineaInicio);
                actual = new ArrayList<>();
                linea++;
                lineaInicio = linea;
            } else {
                campo.append(ch);
            }
        }
        if (campo.length() > 0 || !actual.isEmpty()) {
            actual.add(campo.toString().trim());
            matriz.add(actual);
            numeros.add(lineaInicio);
        }
        return construir(matriz, numeros);
    }

    private static int cuenta(String s, char c) {
        int n = 0;
        for (char x : s.toCharArray()) {
            if (x == c) {
                n++;
            }
        }
        return n;
    }

    /** UTF-8 si es válido; si no, Windows-1252 (CSV típico de Excel en español). */
    private static String decodificar(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }

    // ------------------------------------------------------------------ comunes

    private static Tabla construir(List<List<String>> matriz, List<Integer> numeros) {
        int idxHeader = -1;
        for (int i = 0; i < matriz.size(); i++) {
            if (matriz.get(i).stream().anyMatch(s -> !s.isBlank())) {
                idxHeader = i;
                break;
            }
        }
        if (idxHeader < 0) {
            throw new BadRequestException("El archivo está vacío.");
        }
        List<String> headers = new ArrayList<>();
        for (String h : matriz.get(idxHeader)) {
            headers.add(normalizarEncabezado(h));
        }
        List<Fila> filas = new ArrayList<>();
        for (int i = idxHeader + 1; i < matriz.size(); i++) {
            List<String> celdas = matriz.get(i);
            if (celdas.stream().allMatch(String::isBlank)) {
                continue; // fila en blanco
            }
            Map<String, String> valores = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                if (headers.get(c).isEmpty()) {
                    continue;
                }
                valores.put(headers.get(c), c < celdas.size() ? celdas.get(c) : "");
            }
            filas.add(new Fila(numeros.get(i), valores));
            if (filas.size() > MAX_FILAS) {
                throw new BadRequestException("El archivo supera el máximo de " + MAX_FILAS + " registros por carga.");
            }
        }
        return new Tabla(headers, filas);
    }

    /** "Precio de Compra" -> precio_compra (sin tildes, minúsculas, guion bajo). */
    public static String normalizarEncabezado(String h) {
        String s = Normalizer.normalize(h == null ? "" : h, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return switch (s) {
            case "precio_de_compra", "costo", "precio_costo" -> "precio_compra";
            case "precio_de_venta", "precio_publico", "precio_venta_publico", "pvp" -> "precio_venta";
            case "invima", "registro_sanitario" -> "registro_invima";
            case "iva", "impuesto", "iva_" -> "iva_porcentaje";
            case "cantidad_inicial", "existencias", "stock", "unidades" -> "cantidad";
            case "stock_min", "minimo", "stock_minimo_permitido" -> "stock_minimo";
            case "lote" -> "numero_lote";
            case "fecha_ingreso", "fecha_de_compra" -> "fecha_compra";
            case "fecha_vence", "vencimiento", "fecha_de_vencimiento" -> "fecha_vencimiento";
            case "proximo_pedido", "fecha_proximo_pedido_" -> "fecha_proximo_pedido";
            default -> s;
        };
    }
}
