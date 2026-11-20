package com.uniminuto.clinica.util;

import com.uniminuto.clinica.models.FacturaDocumento;
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
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Dibuja la factura y la prefactura con la identidad visual de la clínica. */
public final class FacturaPdf implements AutoCloseable {
    private static final float MARGEN = 36;
    private static final float ANCHO = 540;
    private static final float PIE = 54;
    private static final Color AZUL = new Color(14, 55, 85);
    private static final Color TINTA = new Color(28, 47, 60);
    private static final Color GRIS = new Color(77, 96, 110);
    private static final Color LINEA = new Color(207, 221, 230);
    private static final Color FONDO = new Color(239, 246, 250);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final float[] COLUMNAS = {70, 144, 34, 78, 82, 42, 90};
    private static final String[] TITULOS = {"Tipo", "Descripción", "Cant.", "Precio unitario", "Subtotal", "IVA %", "IVA"};

    private final FacturaDocumento factura;
    private final PDDocument pdf = new PDDocument();
    private final PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final NumberFormat numero = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"));
    private PDPageContentStream pagina;
    private float y;
    private int paginaNumero;

    /** Configura el documento y el formato de importes en pesos colombianos. */
    private FacturaPdf(FacturaDocumento factura) {
        this.factura = factura;
        numero.setMinimumFractionDigits(2);
        numero.setMaximumFractionDigits(2);
    }

    /** Genera el archivo con cabecera clínica, conceptos, impuestos y resumen de pago. */
    public static byte[] generar(FacturaDocumento factura) throws IOException {
        try (var formato = new FacturaPdf(factura); var salida = new ByteArrayOutputStream()) {
            formato.nuevaPagina();
            formato.cabecera();
            formato.datosClinicos();
            formato.detalle();
            formato.resumen();
            formato.cerrarPagina();
            formato.pdf.save(salida);
            return salida.toByteArray();
        }
    }

    /** Abre una página e identifica el documento en las hojas de continuación. */
    private void nuevaPagina() throws IOException {
        cerrarPagina();
        var hoja = new PDPage(PDRectangle.LETTER);
        pdf.addPage(hoja);
        pagina = new PDPageContentStream(pdf, hoja);
        paginaNumero++;
        y = hoja.getMediaBox().getHeight() - MARGEN;
        if (paginaNumero > 1) {
            texto(tipo() + " N.º " + campo("numero_factura"), MARGEN, y - 13, 13, true, AZUL);
            y -= 32;
        }
    }

    /** Imprime el logo y los datos ya configurados para la clínica y el documento. */
    private void cabecera() throws IOException {
        var clinica = factura.clinica();
        var datos = new ArrayList<String>();
        datos.addAll(envolver(clinica.getOrDefault("CLINICA_NOMBRE", "Clínica Veterinaria Uniminuto"), 286, 15, true));
        int lineasNombre = datos.size();
        for (String campo : List.of("NIT", "DIRECCION", "TELEFONO", "EMAIL")) {
            String valor = clinica.get("CLINICA_" + campo);
            if (valor != null && !valor.isBlank()) {
                datos.addAll(envolver(("NIT".equals(campo) ? "NIT " : "") + valor, 286, 9, false));
            }
        }
        float alto = Math.max(96, 25 + lineasNombre * 18 + (datos.size() - lineasNombre) * 12);
        caja(MARGEN, y, ANCHO, alto, FONDO);
        try (var logo = FacturaPdf.class.getResourceAsStream("/pdf/logo-vet.jpg")) {
            if (logo != null) {
                var imagen = PDImageXObject.createFromByteArray(pdf, logo.readAllBytes(), "Logo de la clínica");
                float factor = Math.min(46f / imagen.getWidth(), 46f / imagen.getHeight());
                pagina.drawImage(imagen, MARGEN + 12, y - 60, imagen.getWidth() * factor, imagen.getHeight() * factor);
            }
        }
        float posicion = y - 24;
        for (int i = 0; i < datos.size(); i++) {
            boolean nombre = i < lineasNombre;
            texto(datos.get(i), MARGEN + 66, posicion, nombre ? 15 : 9, nombre, nombre ? AZUL : GRIS);
            posicion -= nombre ? 18 : 12;
        }
        float derecha = MARGEN + 366;
        trazo(derecha, y - 12, derecha, y - alto + 12);
        texto(tipo(), derecha + 12, y - 25, 10, true, AZUL);
        String numero = "N.º " + campo("numero_factura");
        float anchoNumero = MARGEN + ANCHO - derecha - 24;
        float tamanoNumero = 14 * Math.min(1f, anchoNumero / medir(numero, 14, true));
        texto(numero, derecha + 12, y - 48, tamanoNumero, true, TINTA);
        texto("Emisión (Bogotá)", derecha + 12, y - 68, 8, true, GRIS);
        texto(fecha(factura.cabecera().get("fecha_emision")), derecha + 12, y - 81, 9, false, TINTA);
        y -= alto + 12;
    }

    /** Presenta por separado al propietario, paciente y profesional de la cita. */
    private void datosClinicos() throws IOException {
        float ancho = (ANCHO - 12) / 2;
        var izquierda = datosTarjeta(ancho, "Propietario: " + campo("propietario"),
                "Identificación: " + campo("documento_propietario"), "Mascota: " + campo("mascota"),
                "Especie y raza: " + campo("identificacion_mascota"));
        var derecha = datosTarjeta(ancho, "Veterinario: " + campo("medico"),
                "Especialidad: " + campo("especialidad"), "Registro profesional: " + campo("registro_profesional"),
                "Estado: " + estado());
        float alto = 34 + Math.max(izquierda.size(), derecha.size()) * 13f;
        tarjeta(MARGEN, ancho, alto, "PROPIETARIO Y MASCOTA", izquierda);
        tarjeta(MARGEN + ancho + 12, ancho, alto, "ATENCIÓN VETERINARIA", derecha);
        y -= alto + 20;
    }

    /** Ajusta las líneas a la anchura de la tarjeta de información. */
    private List<String> datosTarjeta(float ancho, String... datos) throws IOException {
        var lineas = new ArrayList<String>();
        for (var dato : datos) lineas.addAll(envolver(dato, ancho - 24, 9, false));
        return lineas;
    }

    /** Dibuja una tarjeta con su encabezado y sus datos. */
    private void tarjeta(float x, float ancho, float alto, String titulo, List<String> datos) throws IOException {
        caja(x, y, ancho, alto, Color.WHITE);
        texto(titulo, x + 12, y - 19, 9, true, AZUL);
        float linea = y - 36;
        for (var dato : datos) {
            texto(dato, x + 12, linea, 9, false, TINTA);
            linea -= 13;
        }
    }

    /** Dibuja el detalle y repite las columnas si la tabla continúa en otra página. */
    private void detalle() throws IOException {
        encabezadosDetalle();
        int indice = 0;
        for (var item : factura.lineas()) {
            var descripcion = envolver(item.descripcion(), COLUMNAS[1] - 14, 9, false);
            int desde = 0;
            while (desde < descripcion.size()) {
                int caben = (int) ((y - PIE - 12) / 12);
                if (caben < 1 || descripcion.size() - desde > caben && desde == 0 && y < 425) {
                    nuevaPagina();
                    encabezadosDetalle();
                    caben = (int) ((y - PIE - 12) / 12);
                }
                int hasta = Math.min(descripcion.size(), desde + caben);
                float alto = 12 + (hasta - desde) * 12;
                caja(MARGEN, y, ANCHO, alto, indice % 2 == 0 ? Color.WHITE : FONDO);
                for (int i = desde; i < hasta; i++) {
                    texto(descripcion.get(i), MARGEN + COLUMNAS[0] + 7, y - 15 - (i - desde) * 12, 9, false, TINTA);
                }
                if (desde == 0) {
                    String tipo = "MEDICAMENTO".equals(item.tipo()) ? "Medicamento" : "Servicio";
                    texto(tipo, MARGEN + 7, y - 15, 9, false, TINTA);
                    String[] valores = {Integer.toString(item.cantidad()), moneda(item.precioUnitario()), moneda(item.subtotal()),
                            porcentaje(item.porcentajeIva()), moneda(item.iva())};
                    float borde = MARGEN + COLUMNAS[0] + COLUMNAS[1];
                    for (int col = 2; col < COLUMNAS.length; col++) {
                        borde += COLUMNAS[col];
                        textoDerecha(valores[col - 2], borde - 7, y - 15, 9, false, TINTA);
                    }
                }
                y -= alto;
                desde = hasta;
            }
            indice++;
        }
        y -= 18;
    }

    /** Identifica las columnas de servicios, medicamentos e impuestos. */
    private void encabezadosDetalle() throws IOException {
        texto("DETALLE DE SERVICIOS Y MEDICAMENTOS", MARGEN, y - 11, 10, true, AZUL);
        y -= 20;
        caja(MARGEN, y, ANCHO, 24, AZUL);
        float x = MARGEN;
        for (int i = 0; i < TITULOS.length; i++) {
            if (i < 2) texto(TITULOS[i], x + 7, y - 15, 9, true, Color.WHITE);
            else textoDerecha(TITULOS[i], x + COLUMNAS[i] - 7, y - 15, 9, true, Color.WHITE);
            x += COLUMNAS[i];
        }
        y -= 24;
    }

    /** Muestra el resumen de pago y agrupa el IVA registrado por porcentaje. */
    private void resumen() throws IOException {
        Map<BigDecimal, BigDecimal> impuestos = new TreeMap<>();
        for (var linea : factura.lineas()) {
            if (linea.porcentajeIva().signum() > 0) {
                impuestos.merge(linea.porcentajeIva(), linea.iva(), BigDecimal::add);
            }
        }
        var montos = new ArrayList<>(List.of(
                Map.entry("Subtotal", importe("subtotal")),
                Map.entry("IVA", importe("iva")),
                Map.entry("Descuento", importe("descuento")),
                Map.entry("Total a pagar", importe("total")),
                Map.entry("Pagado", factura.pagado())));
        if (factura.saldo() != null) montos.add(Map.entry("Saldo pendiente", factura.saldo()));
        float altoTotales = 34 + montos.size() * 20f;
        if (y - altoTotales < PIE) nuevaPagina();

        float anchoImpuestos = 230;
        float xTotales = MARGEN + anchoImpuestos + 14;
        caja(xTotales, y, ANCHO - anchoImpuestos - 14, altoTotales, FONDO);
        texto("RESUMEN DE PAGO · COP", xTotales + 12, y - 19, 10, true, AZUL);
        float linea = y - 38;
        for (var monto : montos) {
            boolean destacado = "Total a pagar".equals(monto.getKey()) || "Saldo pendiente".equals(monto.getKey());
            texto(monto.getKey(), xTotales + 12, linea, destacado ? 11 : 9, destacado, destacado ? AZUL : TINTA);
            textoDerecha(moneda(monto.getValue()), MARGEN + ANCHO - 12, linea,
                    destacado ? 11 : 9, destacado, destacado ? AZUL : TINTA);
            linea -= 20;
        }

        float limiteTotales = y - altoTotales;
        encabezadosImpuestos(anchoImpuestos);
        if (impuestos.isEmpty()) {
            texto("Sin IVA aplicado en las líneas.", MARGEN + 8, y - 15, 9, false, GRIS);
            y -= 22;
        }
        for (var impuesto : impuestos.entrySet()) {
            if (y - 22 < PIE) {
                nuevaPagina();
                limiteTotales = y;
                encabezadosImpuestos(anchoImpuestos);
            }
            caja(MARGEN, y, anchoImpuestos, 22, Color.WHITE);
            texto("IVA " + porcentaje(impuesto.getKey()), MARGEN + 8, y - 15, 9, false, TINTA);
            textoDerecha(moneda(impuesto.getValue()), MARGEN + anchoImpuestos - 8, y - 15, 9, false, TINTA);
            y -= 22;
        }

        Object motivo = factura.cabecera().get("motivo_sin_pago");
        y = Math.min(y, limiteTotales) - 14;
        if (motivo != null) {
            for (String lineaMotivo : envolver("Motivo del valor cero: " + motivo, ANCHO, 9, false)) {
                if (y - 12 < PIE) nuevaPagina();
                texto(lineaMotivo, MARGEN, y, 9, false, TINTA);
                y -= 12;
            }
        }
    }

    /** Presenta los encabezados de la tabla de impuestos. */
    private void encabezadosImpuestos(float ancho) throws IOException {
        texto("DISCRIMINACIÓN DE IMPUESTOS", MARGEN, y - 11, 9, true, AZUL);
        y -= 19;
        caja(MARGEN, y, ancho, 23, FONDO);
        texto("Impuesto", MARGEN + 8, y - 15, 9, true, AZUL);
        textoDerecha("Valor", MARGEN + ancho - 8, y - 15, 9, true, AZUL);
        y -= 23;
    }

    /** Añade el pie y termina la página que se está dibujando. */
    private void cerrarPagina() throws IOException {
        if (pagina == null) return;
        trazo(MARGEN, 42, MARGEN + ANCHO, 42);
        texto("Generado " + FECHA.format(factura.generado()) + " · Bogotá", MARGEN, 28, 8, false, GRIS);
        textoDerecha("Página " + paginaNumero, MARGEN + ANCHO, 28, 8, false, GRIS);
        pagina.close();
        pagina = null;
    }

    /** Cierra los recursos del documento aunque se produzca un error al dibujar. */
    @Override
    public void close() throws IOException {
        try {
            if (pagina != null) pagina.close();
        } finally {
            pdf.close();
        }
    }

    /** Divide los textos largos para que no se salgan de la columna. */
    private List<String> envolver(String valor, float ancho, float tamano, boolean fuerte) throws IOException {
        String limpio = textoCompatible(valor);
        var lineas = new ArrayList<String>();
        String linea = "";
        for (String palabra : limpio.split(" ")) {
            var candidata = linea.isEmpty() ? palabra : linea + " " + palabra;
            if (medir(candidata, tamano, fuerte) <= ancho) {
                linea = candidata;
                continue;
            }
            if (!linea.isEmpty()) lineas.add(linea);
            linea = "";
            for (char caracter : palabra.toCharArray()) {
                if (medir(linea + caracter, tamano, fuerte) > ancho) {
                    lineas.add(linea);
                    linea = "";
                }
                linea += caracter;
            }
        }
        if (!linea.isEmpty() || lineas.isEmpty()) lineas.add(linea);
        return lineas;
    }

    /** Escribe texto alineado al margen de la celda. */
    private void texto(String valor, float x, float base, float tamano, boolean fuerte, Color color) throws IOException {
        pagina.beginText();
        pagina.setFont(fuerte ? negrita : normal, tamano);
        pagina.setNonStrokingColor(color);
        pagina.newLineAtOffset(x, base);
        pagina.showText(textoCompatible(valor));
        pagina.endText();
    }

    /** Alinea valores y rótulos numéricos al borde derecho. */
    private void textoDerecha(String valor, float x, float base, float tamano, boolean fuerte, Color color) throws IOException {
        texto(valor, x - medir(valor, tamano, fuerte), base, tamano, fuerte, color);
    }

    /** Calcula el ancho del texto tal como se imprime en el PDF. */
    private float medir(String valor, float tamano, boolean fuerte) throws IOException {
        return (fuerte ? negrita : normal).getStringWidth(textoCompatible(valor)) * tamano / 1000;
    }

    /** Conserva los acentos y sustituye caracteres fuera de la fuente. */
    private String textoCompatible(String valor) throws IOException {
        var limpio = new StringBuilder();
        for (int punto : valor.replaceAll("\\s+", " ").strip().codePoints().toArray()) {
            var caracter = Character.toString(punto);
            try {
                normal.encode(caracter);
                limpio.append(caracter);
            } catch (IllegalArgumentException noCompatible) {
                limpio.append('?');
            }
        }
        return limpio.toString();
    }

    /** Pinta el fondo y el borde de un bloque. */
    private void caja(float x, float superior, float ancho, float alto, Color color) throws IOException {
        pagina.setNonStrokingColor(color);
        pagina.setStrokingColor(LINEA);
        pagina.setLineWidth(0.5f);
        pagina.addRect(x, superior - alto, ancho, alto);
        pagina.fillAndStroke();
    }

    /** Dibuja una línea fina para separar secciones. */
    private void trazo(float x1, float y1, float x2, float y2) throws IOException {
        pagina.setStrokingColor(LINEA);
        pagina.setLineWidth(0.6f);
        pagina.moveTo(x1, y1);
        pagina.lineTo(x2, y2);
        pagina.stroke();
    }

    /** Devuelve el nombre que se presenta para el tipo de documento. */
    private String tipo() {
        return "PREFACTURA".equals(campo("tipo_documento")) ? "PREFACTURA" : "FACTURA DE SERVICIOS";
    }

    /** Presenta de forma legible el estado de pago almacenado. */
    private String estado() {
        return switch (campo("estado")) {
            case "NO_REQUIERE_PAGO" -> "No requiere pago";
            case "PAGO_PARCIAL" -> "Pago parcial";
            case "PENDIENTE" -> "Pendiente de pago";
            case "PAGADA" -> "Pagada";
            case "ANULADA" -> "Anulada";
            default -> campo("estado");
        };
    }

    /** Obtiene un dato de cabecera y representa los valores ausentes. */
    private String campo(String clave) {
        Object valor = factura.cabecera().get(clave);
        return valor == null || valor.toString().isBlank() ? "—" : valor.toString();
    }

    /** Lee el importe exacto almacenado en la cabecera. */
    private BigDecimal importe(String clave) {
        return (BigDecimal) factura.cabecera().get(clave);
    }

    /** Presenta un importe en pesos colombianos. */
    private String moneda(BigDecimal valor) {
        return "$ " + numero.format(valor);
    }

    /** Presenta el porcentaje sin ceros decimales innecesarios. */
    private String porcentaje(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString().replace('.', ',') + " %";
    }

    /** Formatea la fecha de emisión almacenada en el documento. */
    private String fecha(Object fecha) {
        if (fecha instanceof LocalDateTime local) return FECHA.format(local);
        if (fecha instanceof java.sql.Timestamp marca) return FECHA.format(marca.toLocalDateTime());
        return fecha == null ? "—" : fecha.toString();
    }
}
