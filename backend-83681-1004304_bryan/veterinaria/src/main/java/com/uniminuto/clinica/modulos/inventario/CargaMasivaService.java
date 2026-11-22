package com.uniminuto.clinica.modulos.inventario;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.modulos.inventario.LectorArchivo.Fila;
import com.uniminuto.clinica.modulos.inventario.LectorArchivo.Tabla;
import com.uniminuto.clinica.security.CurrentUser;
import com.uniminuto.clinica.support.Db;
import com.uniminuto.clinica.support.In;
import com.uniminuto.clinica.support.XlsxKit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Carga masiva de inventario (.xlsx / .csv) en dos pasos:
 * 1) previsualizar: valida estructura y cada fila, guarda el log y devuelve correctos/errores;
 * 2) confirmar: importa SOLO las filas válidas, cada una en su propia transacción, de modo que
 *    un error en un registro no afecta a los demás.
 */
@Service
public class CargaMasivaService {

    static final List<String> REQUERIDOS = List.of("nombre", "tipo", "registro_invima", "precio_compra",
            "precio_venta", "cantidad", "stock_minimo", "fecha_compra", "fecha_vencimiento");
    static final List<String> OPCIONALES = List.of("codigo", "descripcion", "presentacion", "iva_porcentaje",
            "numero_lote", "fecha_proximo_pedido");

    private final Db db;
    private final CurrentUser current;
    private final InventarioService inventario;
    private final ObjectMapper mapper;
    private final TransactionTemplate tx;

    public CargaMasivaService(Db db, CurrentUser current, InventarioService inventario, ObjectMapper mapper,
                              PlatformTransactionManager txManager) {
        this.db = db;
        this.current = current;
        this.inventario = inventario;
        this.mapper = mapper;
        this.tx = new TransactionTemplate(txManager);
    }

    // ------------------------------------------------------------------ paso 1: vista previa

    @Transactional
    public Map<String, Object> previsualizar(MultipartFile archivo) {
        Tabla tabla = LectorArchivo.leer(archivo);
        List<String> faltantes = new ArrayList<>();
        for (String r : REQUERIDOS) {
            if (!tabla.encabezados().contains(r)) {
                faltantes.add(r);
            }
        }
        if (!faltantes.isEmpty()) {
            throw new BadRequestException("La estructura del archivo no es válida. Faltan las columnas: "
                    + String.join(", ", faltantes) + ". Descargue la plantilla para ver el formato esperado.");
        }
        if (tabla.filas().isEmpty()) {
            throw new BadRequestException("El archivo no contiene registros para cargar.");
        }
        String nombre = archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename();
        String tipoArchivo = nombre.toLowerCase(Locale.ROOT).endsWith(".csv") ? "CSV" : "XLSX";
        long cargaId = db.insert("""
                INSERT INTO carga_inventario (nombre_archivo, tipo_archivo, usuario_id, total_registros, estado)
                VALUES (?,?,?,?, 'PREVISUALIZADA')
                """, nombre, tipoArchivo, current.get().id(), tabla.filas().size());

        Set<String> nombresArchivo = new HashSet<>();
        Set<String> codigosArchivo = new HashSet<>();
        int validos = 0;
        int errores = 0;
        List<Map<String, Object>> salida = new ArrayList<>();
        for (Fila f : tabla.filas()) {
            List<String> errs = validarFila(f.valores(), nombresArchivo, codigosArchivo);
            boolean ok = errs.isEmpty();
            if (ok) {
                validos++;
            } else {
                errores++;
            }
            String motivo = ok ? null : String.join("; ", errs);
            db.insert("INSERT INTO carga_inventario_detalle (carga_id, fila, datos, valido, errores) VALUES (?,?,?,?,?)",
                    cargaId, f.numero(), json(f.valores()), ok, motivo);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fila", f.numero());
            item.put("valido", ok);
            item.put("errores", motivo);
            item.put("datos", f.valores());
            salida.add(item);
        }
        db.update("UPDATE carga_inventario SET registros_validos = ?, registros_error = ? WHERE id = ?",
                validos, errores, cargaId);
        Map<String, Object> rs = new LinkedHashMap<>();
        rs.put("cargaId", cargaId);
        rs.put("archivo", nombre);
        rs.put("total", tabla.filas().size());
        rs.put("validos", validos);
        rs.put("errores", errores);
        rs.put("filas", salida);
        return rs;
    }

    /**
     * Valida una fila completa y devuelve todos los motivos de rechazo (lista vacía = fila correcta).
     * Reglas: obligatorios, formatos numéricos/fechas, Invima, duplicados en el archivo y en la BD.
     */
    List<String> validarFila(Map<String, String> v, Set<String> nombresArchivo, Set<String> codigosArchivo) {
        List<String> errs = new ArrayList<>();
        Map<String, Object> normal = normalizar(v, errs);
        if (!errs.isEmpty()) {
            return errs;
        }
        try {
            inventario.validar(normal, null);
        } catch (BadRequestException e) {
            errs.add(e.getMessage());
        }
        // Duplicados dentro del propio archivo
        String nombreKey = String.valueOf(normal.get("nombre")).toLowerCase(Locale.ROOT);
        if (!nombresArchivo.add(nombreKey)) {
            errs.add("Medicamento duplicado dentro del archivo (nombre '" + normal.get("nombre") + "').");
        }
        Object codigo = normal.get("codigo");
        if (codigo != null && !codigosArchivo.add(String.valueOf(codigo).toUpperCase(Locale.ROOT))) {
            errs.add("Código duplicado dentro del archivo (" + codigo + ").");
        }
        return errs;
    }

    /** Convierte los textos del archivo a los campos del modelo y valida los formatos de números y fechas. */
    private Map<String, Object> normalizar(Map<String, String> v, List<String> errs) {
        Map<String, Object> n = new LinkedHashMap<>();
        for (String campo : List.of("nombre", "tipo", "registro_invima", "precio_compra", "precio_venta", "cantidad",
                "stock_minimo", "fecha_compra", "fecha_vencimiento")) {
            String s = v.get(campo);
            if (campo.equals("registro_invima") || campo.equals("fecha_vencimiento")) {
                continue; // su obligatoriedad depende del tipo
            }
            if (s == null || s.isBlank()) {
                errs.add("El campo '" + campo + "' es obligatorio.");
            }
        }
        String tipo = val(v, "tipo");
        if (tipo != null) {
            tipo = tipo.toUpperCase(Locale.ROOT);
        }
        n.put("nombre", val(v, "nombre"));
        n.put("tipo", tipo);
        n.put("codigo", val(v, "codigo"));
        n.put("descripcion", val(v, "descripcion"));
        n.put("presentacion", val(v, "presentacion"));
        n.put("registroInvima", val(v, "registro_invima"));
        n.put("precioCompra", decimal(v, "precio_compra", errs));
        n.put("precioVenta", decimal(v, "precio_venta", errs));
        n.put("ivaPorcentaje", val(v, "iva_porcentaje") == null ? BigDecimal.ZERO : decimal(v, "iva_porcentaje", errs));
        n.put("stockMinimo", entero(v, "stock_minimo", errs));
        n.put("cantidad", entero(v, "cantidad", errs));
        n.put("numeroLote", val(v, "numero_lote"));
        n.put("fechaProximoPedido", fecha(v, "fecha_proximo_pedido", errs));
        LocalDate compra = fecha(v, "fecha_compra", errs);
        LocalDate vence = fecha(v, "fecha_vencimiento", errs);
        n.put("fechaIngreso", compra);
        n.put("fechaVencimiento", vence);

        Object cant = n.get("cantidad");
        if (cant instanceof Integer c && c < 0) {
            errs.add("La cantidad no puede ser negativa.");
        }
        if (compra != null && compra.isAfter(LocalDate.now())) {
            errs.add("La fecha de compra no puede ser futura.");
        }
        if ("MEDICAMENTO".equals(tipo) && vence == null && val(v, "fecha_vencimiento") == null) {
            errs.add("La fecha de vencimiento es obligatoria para medicamentos.");
        }
        if (vence != null && compra != null && vence.isBefore(compra)) {
            errs.add("La fecha de vencimiento es anterior a la fecha de compra.");
        }
        if (vence != null && !vence.isAfter(LocalDate.now())) {
            errs.add("El producto ya está vencido (" + vence + ").");
        }
        return n;
    }

    private static String val(Map<String, String> v, String k) {
        String s = v.get(k);
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static BigDecimal decimal(Map<String, String> v, String k, List<String> errs) {
        String s = val(v, k);
        if (s == null) {
            return null;
        }
        try {
            return In.parseDec(s, k);
        } catch (BadRequestException e) {
            errs.add("El valor '" + s + "' de '" + k + "' no es un número válido.");
            return null;
        }
    }

    private static Integer entero(Map<String, String> v, String k, List<String> errs) {
        String s = val(v, k);
        if (s == null) {
            return null;
        }
        try {
            return new BigDecimal(s.replace(',', '.')).intValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            errs.add("El valor '" + s + "' de '" + k + "' debe ser un número entero.");
            return null;
        }
    }

    private static LocalDate fecha(Map<String, String> v, String k, List<String> errs) {
        String s = val(v, k);
        if (s == null) {
            return null;
        }
        try {
            return In.parseDate(s, k);
        } catch (BadRequestException e) {
            errs.add("La fecha '" + s + "' de '" + k + "' no es válida (use AAAA-MM-DD o DD/MM/AAAA).");
            return null;
        }
    }

    // ------------------------------------------------------------------ paso 2: confirmación

    public Map<String, Object> confirmar(long cargaId) {
        Map<String, Object> carga = db.must("La carga no existe.", "SELECT * FROM carga_inventario WHERE id = ?", cargaId);
        if (!"PREVISUALIZADA".equals(carga.get("estado"))) {
            throw new BadRequestException("La carga ya fue " + String.valueOf(carga.get("estado")).toLowerCase(Locale.ROOT)
                    + " y no puede confirmarse de nuevo.");
        }
        // Marca la carga como en proceso para impedir una doble confirmación concurrente.
        int tomada = db.update("UPDATE carga_inventario SET estado = 'CONFIRMADA', fecha_confirmacion = NOW() "
                + "WHERE id = ? AND estado = 'PREVISUALIZADA'", cargaId);
        if (tomada == 0) {
            throw new BadRequestException("La carga ya está siendo procesada.");
        }
        List<Map<String, Object>> filas = db.list("SELECT id, fila, datos FROM carga_inventario_detalle "
                + "WHERE carga_id = ? AND valido = 1 AND importado = 0 ORDER BY fila", cargaId);
        int importados = 0;
        int fallidos = 0;
        Set<String> nombresArchivo = new HashSet<>();
        Set<String> codigosArchivo = new HashSet<>();
        long usuarioId = current.get().id();
        for (Map<String, Object> f : filas) {
            long detalleId = In.toLong(f.get("id"));
            try {
                Map<String, String> datos = leerJson(String.valueOf(f.get("datos")));
                // Revalida con el estado actual de la BD (otro usuario pudo crear el mismo medicamento).
                List<String> errs = validarFila(datos, nombresArchivo, codigosArchivo);
                if (!errs.isEmpty()) {
                    throw new BadRequestException(String.join("; ", errs));
                }
                tx.executeWithoutResult(status -> importarFila(datos, cargaId, In.toInt(f.get("fila"))));
                db.update("UPDATE carga_inventario_detalle SET importado = 1 WHERE id = ?", detalleId);
                importados++;
            } catch (RuntimeException e) {
                fallidos++;
                db.update("UPDATE carga_inventario_detalle SET valido = 0, errores = ? WHERE id = ?",
                        recortar("No se importó: " + e.getMessage(), 1000), detalleId);
            }
        }
        db.update("UPDATE carga_inventario SET registros_importados = ?, "
                        + "registros_validos = registros_validos - ?, registros_error = registros_error + ? WHERE id = ?",
                importados, fallidos, fallidos, cargaId);
        Map<String, Object> rs = new LinkedHashMap<>(db.one("SELECT * FROM carga_inventario WHERE id = ?", cargaId));
        rs.put("usuarioConfirma", usuarioId);
        return rs;
    }

    /** Importa una fila dentro de su propia transacción. */
    private void importarFila(Map<String, String> v, long cargaId, int fila) {
        List<String> ignorar = new ArrayList<>();
        Map<String, Object> n = normalizar(v, ignorar);
        InventarioService.Datos datos = inventario.validar(n, null);
        long medId = inventario.insertarMedicamento(datos, (LocalDate) n.get("fechaIngreso"));
        Object cant = n.get("cantidad");
        int cantidad = cant instanceof Integer c ? c : 0;
        if (cantidad > 0) {
            Map<String, Object> entrada = new LinkedHashMap<>();
            entrada.put("cantidad", cantidad);
            entrada.put("numeroLote", n.get("numeroLote") != null ? n.get("numeroLote") : "C" + cargaId + "-F" + fila);
            entrada.put("fechaIngreso", n.get("fechaIngreso") == null ? null : n.get("fechaIngreso").toString());
            entrada.put("fechaVencimiento", n.get("fechaVencimiento") == null ? null : n.get("fechaVencimiento").toString());
            entrada.put("precioCompra", n.get("precioCompra"));
            inventario.registrarEntrada(medId, entrada, cargaId);
        }
    }

    @Transactional
    public Map<String, Object> cancelar(long cargaId) {
        Map<String, Object> carga = db.must("La carga no existe.", "SELECT estado FROM carga_inventario WHERE id = ?", cargaId);
        if (!"PREVISUALIZADA".equals(carga.get("estado"))) {
            throw new BadRequestException("Solo se pueden cancelar cargas que aún no se han confirmado.");
        }
        db.update("UPDATE carga_inventario SET estado = 'CANCELADA' WHERE id = ?", cargaId);
        return db.one("SELECT * FROM carga_inventario WHERE id = ?", cargaId);
    }

    // ------------------------------------------------------------------ consultas y reportes

    /** Log de cargues: fecha, usuario y resultados. */
    public List<Map<String, Object>> log() {
        return db.list("""
                SELECT c.id, c.nombre_archivo, c.tipo_archivo, c.fecha_carga, c.fecha_confirmacion, c.total_registros,
                       c.registros_validos, c.registros_error, c.registros_importados, c.estado, u.username AS usuario
                  FROM carga_inventario c JOIN usuario u ON u.id = c.usuario_id
                 ORDER BY c.id DESC
                """);
    }

    public Map<String, Object> detalle(long cargaId) {
        Map<String, Object> c = db.must("La carga no existe.", """
                SELECT c.*, u.username AS usuario FROM carga_inventario c JOIN usuario u ON u.id = c.usuario_id
                 WHERE c.id = ?
                """, cargaId);
        List<Map<String, Object>> filas = db.list(
                "SELECT fila, valido, importado, errores, datos FROM carga_inventario_detalle WHERE carga_id = ? ORDER BY fila", cargaId);
        Map<String, Object> rs = new LinkedHashMap<>(c);
        rs.put("filas", filas);
        return rs;
    }

    /** Informe de errores en Excel: fila del archivo y motivo del rechazo. */
    public byte[] informeErrores(long cargaId) {
        Map<String, Object> c = db.must("La carga no existe.", "SELECT nombre_archivo FROM carga_inventario WHERE id = ?", cargaId);
        List<Map<String, Object>> filas = db.list(
                "SELECT fila, errores, datos FROM carga_inventario_detalle WHERE carga_id = ? AND valido = 0 ORDER BY fila", cargaId);
        List<List<Object>> rows = new ArrayList<>();
        for (Map<String, Object> f : filas) {
            Map<String, String> datos = leerJson(String.valueOf(f.get("datos")));
            rows.add(List.of(f.get("fila"), String.valueOf(f.get("errores")),
                    datos.getOrDefault("nombre", ""), datos.getOrDefault("tipo", ""),
                    datos.getOrDefault("cantidad", ""), datos.getOrDefault("fecha_vencimiento", "")));
        }
        return XlsxKit.sheet("Errores", "Informe de errores de carga #" + cargaId + " - " + c.get("nombreArchivo"),
                "Generado: " + com.uniminuto.clinica.support.PdfKit.now() + " | Registros con error: " + rows.size(),
                List.of("Fila del archivo", "Motivo del rechazo", "Nombre", "Tipo", "Cantidad", "Fecha vencimiento"),
                rows, null);
    }

    /** Plantilla .xlsx con los encabezados esperados y filas de ejemplo. */
    public byte[] plantilla() {
        List<String> headers = new ArrayList<>();
        headers.addAll(List.of("codigo", "nombre", "tipo", "descripcion", "presentacion", "registro_invima",
                "precio_compra", "precio_venta", "iva_porcentaje", "cantidad", "stock_minimo", "numero_lote",
                "fecha_compra", "fecha_vencimiento", "fecha_proximo_pedido"));
        LocalDate hoy = LocalDate.now();
        List<List<Object>> ej = List.of(
                List.of("", "Amoxicilina 500mg", "MEDICAMENTO", "Antibiótico", "Caja x 20", "INVIMA 2023M-000123",
                        "12000", "18000", "0", "50", "10", "L-2026-01", hoy.minusDays(5).toString(),
                        hoy.plusMonths(18).toString(), hoy.plusMonths(2).toString()),
                List.of("", "Termómetro digital", "PRODUCTO", "Utensilio clínico", "Unidad", "",
                        "8000", "15000", "19", "20", "5", "L-2026-02", hoy.minusDays(5).toString(), "", ""));
        return XlsxKit.sheet("Plantilla", null, null, headers, new ArrayList<>(ej), null);
    }

    // ------------------------------------------------------------------ JSON

    private String json(Map<String, String> datos) {
        return mapper.writeValueAsString(datos);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> leerJson(String s) {
        Map<String, Object> raw = mapper.readValue(s, Map.class);
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            out.put(e.getKey(), e.getValue() == null ? "" : String.valueOf(e.getValue()));
        }
        return out;
    }

    private static String recortar(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
