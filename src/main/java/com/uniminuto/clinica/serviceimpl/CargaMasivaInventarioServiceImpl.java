package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CargaMasivaLog;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.CargaMasivaLogRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.CargaMasivaInventarioService;
import com.uniminuto.clinica.service.CargaMasivaRegistroService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Implementa la previsualización y confirmación de archivos CSV y XLSX. */
@Service
public class CargaMasivaInventarioServiceImpl implements CargaMasivaInventarioService {

    private static final List<String> ENCABEZADOS = List.of("codigo", "nombre", "tipoProducto",
            "registroInvima", "descripcion", "presentacion", "fechaCompra", "fechaVence",
            "precioCompra", "precioVenta", "cantidadActual", "stockMinimo", "fechaProximoPedido",
            "numeroLote", "precioCompraLote");
    private final CargaMasivaLogRepository cargaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final CargaMasivaRegistroService registroService;

    /** Construye el servicio de carga masiva y sus dependencias. */
    public CargaMasivaInventarioServiceImpl(CargaMasivaLogRepository cargaRepository,
                                            UsuarioRepository usuarioRepository,
                                            MedicamentoRepository medicamentoRepository,
                                            CargaMasivaRegistroService registroService) {
        this.cargaRepository = cargaRepository;
        this.usuarioRepository = usuarioRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.registroService = registroService;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Map<String, Object> previsualizar(MultipartFile archivo, String usuarioResponsableId) {
        Usuario usuario = validarUsuario(usuarioResponsableId);
        if (archivo == null || archivo.isEmpty() || archivo.getOriginalFilename() == null) {
            throw new BadRequestException("Debe seleccionar un archivo CSV o XLSX con registros.");
        }
        String archivoNombre = archivo.getOriginalFilename();
        String extension = extension(archivoNombre);
        List<List<String>> filas = extension.equals("csv") ? leerCsv(archivo) : leerXlsx(archivo);
        if (filas.isEmpty()) {
            throw new BadRequestException("El archivo no contiene encabezados ni registros.");
        }

        List<String> encabezadosArchivo = filas.get(0).stream().map(this::normalizarEncabezado).toList();
        List<String> faltantes = ENCABEZADOS.stream().filter(campo -> !encabezadosArchivo.contains(campo)).toList();
        if (!faltantes.isEmpty()) {
            throw new BadRequestException("Faltan encabezados requeridos: " + String.join(", ", faltantes) + ".");
        }
        List<Map<String, String>> validos = new ArrayList<>();
        List<Map<String, Object>> errores = new ArrayList<>();
        Set<String> codigos = new HashSet<>();
        Set<String> nombres = new HashSet<>();
        for (int indice = 1; indice < filas.size(); indice++) {
            List<String> celdas = filas.get(indice);
            if (celdas.stream().allMatch(valor -> valor == null || valor.isBlank())) {
                continue;
            }
            Map<String, String> fila = mapearFila(encabezadosArchivo, celdas);
            String error = validarFila(fila, codigos, nombres);
            if (error == null && medicamentoRepository.existsByCodigoIgnoreCase(fila.get("codigo"))) {
                error = "El código ya existe en el inventario.";
            }
            if (error == null && medicamentoRepository.existsByNombreIgnoreCase(fila.get("nombre"))) {
                error = "El nombre ya existe en el inventario.";
            }
            if (error == null) {
                codigos.add(fila.get("codigo").toUpperCase(Locale.ROOT));
                nombres.add(fila.get("nombre").toLowerCase(Locale.ROOT));
                fila.put("_fila", String.valueOf(indice + 1));
                validos.add(fila);
            } else {
                Map<String, Object> detalleError = new LinkedHashMap<>();
                detalleError.put("fila", indice + 1);
                detalleError.put("motivo", error);
                errores.add(detalleError);
            }
        }
        CargaMasivaLog log = new CargaMasivaLog();
        log.setNombreArchivo(archivoNombre);
        log.setTipoArchivo(extension.toUpperCase(Locale.ROOT));
        log.setTotalRegistros(validos.size() + errores.size());
        log.setRegistrosValidos(validos.size());
        log.setRegistrosErroneos(errores.size());
        log.setEstado("PREVALIDADO");
        log.setUsuario(usuario);
        log.setFechaCarga(java.time.LocalDateTime.now());
        List<Map<String, Object>> datosPrevisualizacion = new ArrayList<>();
        validos.forEach(registro -> {
            Map<String, Object> dato = new LinkedHashMap<>(registro);
            dato.put("_tipo", "REGISTRO");
            datosPrevisualizacion.add(dato);
        });
        errores.forEach(error -> {
            Map<String, Object> dato = new LinkedHashMap<>(error);
            dato.put("_tipo", "ERROR");
            datosPrevisualizacion.add(dato);
        });
        log.setErroresJson(serializar(datosPrevisualizacion));
        log = cargaRepository.save(log);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("cargaId", log.getId());
        resultado.put("estado", log.getEstado());
        resultado.put("totalRegistros", log.getTotalRegistros());
        resultado.put("registrosValidos", log.getRegistrosValidos());
        resultado.put("registrosErroneos", log.getRegistrosErroneos());
        resultado.put("errores", errores);
        resultado.put("registros", validos);
        return resultado;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Map<String, Object> confirmar(Long cargaId, String usuarioResponsableId) {
        Usuario usuario = validarUsuario(usuarioResponsableId);
        if (cargaId == null || cargaId <= 0) {
            throw new BadRequestException("El id de carga debe ser mayor a cero.");
        }
        CargaMasivaLog log = cargaRepository.findLockedById(cargaId)
                .orElseThrow(() -> new BadRequestException("La carga masiva no existe."));
        if (!log.getUsuario().getId().equals(usuario.getId())) {
            throw new BadRequestException("Solo el usuario que previsualizó la carga puede confirmarla.");
        }
        if (!"PREVALIDADO".equals(log.getEstado())) {
            throw new BadRequestException("La carga ya fue confirmada o no está disponible para confirmación.");
        }
        List<Map<String, String>> registros = deserializarRegistros(log.getErroresJson());
        List<Map<String, Object>> errores = deserializarErrores(log.getErroresJson());
        int importados = 0;
        for (Map<String, String> registro : registros) {
            try {
                registroService.guardarRegistro(registro, usuario.getId());
                importados++;
            } catch (RuntimeException exception) {
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("fila", Integer.parseInt(registro.getOrDefault("_fila", "0")));
                error.put("motivo", mensajeSeguro(exception));
                errores.add(error);
            }
        }
        log.setEstado(importados == 0 && !errores.isEmpty() ? "ERROR" : "CONFIRMADO");
        log.setFechaConfirmacion(java.time.LocalDateTime.now());
        log.setRegistrosValidos(importados);
        log.setRegistrosErroneos(errores.size());
        log.setErroresJson(serializar(errores));
        cargaRepository.save(log);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("cargaId", log.getId());
        resultado.put("estado", log.getEstado());
        resultado.put("registrosImportados", importados);
        resultado.put("registrosErroneos", errores.size());
        resultado.put("errores", errores);
        return resultado;
    }

    /** Lee y comprueba que la extensión del archivo sea compatible. */
    private String extension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        String extension = punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (!extension.equals("csv") && !extension.equals("xlsx")) {
            throw new BadRequestException("El archivo debe tener formato CSV o XLSX.");
        }
        return extension;
    }

    /** Lee registros CSV, respetando comillas y comas dentro de campos. */
    private List<List<String>> leerCsv(MultipartFile archivo) {
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            List<List<String>> filas = new ArrayList<>();
            String linea;
            while ((linea = lector.readLine()) != null) {
                filas.add(separarCsv(linea));
            }
            if (!filas.isEmpty() && !filas.get(0).isEmpty()) {
                filas.get(0).set(0, filas.get(0).get(0).replace("\uFEFF", ""));
            }
            return filas;
        } catch (Exception exception) {
            throw new BadRequestException("No fue posible leer el archivo CSV.");
        }
    }

    /** Separa una fila CSV con soporte para comillas escapadas. */
    private List<String> separarCsv(String linea) {
        List<String> celdas = new ArrayList<>();
        StringBuilder celda = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char actual = linea.charAt(i);
            if (actual == '"') {
                if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    celda.append('"');
                    i++;
                } else {
                    entreComillas = !entreComillas;
                }
            } else if (actual == ',' && !entreComillas) {
                celdas.add(celda.toString().trim());
                celda.setLength(0);
            } else {
                celda.append(actual);
            }
        }
        celdas.add(celda.toString().trim());
        return celdas;
    }

    /** Lee el primer sheet de un archivo XLSX. */
    private List<List<String>> leerXlsx(MultipartFile archivo) {
        try (Workbook workbook = WorkbookFactory.create(archivo.getInputStream())) {
            Sheet hoja = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (hoja == null) {
                return List.of();
            }
            DataFormatter formatter = new DataFormatter(Locale.ROOT);
            List<List<String>> filas = new ArrayList<>();
            for (Row fila : hoja) {
                List<String> celdas = new ArrayList<>();
                for (int columna = 0; columna < Math.max(fila.getLastCellNum(), 0); columna++) {
                    Cell celda = fila.getCell(columna, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    celdas.add(convertirCelda(celda, formatter));
                }
                filas.add(celdas);
            }
            return filas;
        } catch (Exception exception) {
            throw new BadRequestException("No fue posible leer el archivo XLSX.");
        }
    }

    /** Convierte una celda XLSX a texto compatible con los validadores. */
    private String convertirCelda(Cell celda, DataFormatter formatter) {
        if (celda == null) {
            return "";
        }
        if (celda.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celda)) {
            return DateUtil.getLocalDateTime(celda.getNumericCellValue()).toLocalDate().toString();
        }
        return formatter.formatCellValue(celda).trim();
    }

    /** Normaliza los encabezados y los liga a los valores de una fila. */
    private Map<String, String> mapearFila(List<String> encabezados, List<String> celdas) {
        Map<String, String> fila = new LinkedHashMap<>();
        for (int columna = 0; columna < encabezados.size(); columna++) {
            String valor = columna < celdas.size() ? celdas.get(columna) : "";
            fila.put(encabezados.get(columna), valor == null ? "" : valor.trim());
        }
        return fila;
    }

    /** Comprueba los campos mínimos y duplicados de una fila del archivo. */
    private String validarFila(Map<String, String> fila, Set<String> codigos, Set<String> nombres) {
        String nombre = valor(fila, "nombre");
        String codigo = valor(fila, "codigo").toUpperCase(Locale.ROOT);
        String tipo = valor(fila, "tipoProducto").toUpperCase(Locale.ROOT);
        if (nombre.isBlank() || nombre.length() > 100) return "El nombre es obligatorio y admite máximo 100 caracteres.";
        if (!codigo.matches("[A-Z0-9]{8}")) return "El código debe tener 8 caracteres alfanuméricos.";
        if (codigos.contains(codigo)) return "El código está duplicado dentro del archivo.";
        if (nombres.contains(nombre.toLowerCase(Locale.ROOT))) return "El nombre está duplicado dentro del archivo.";
        if (!List.of("MEDICAMENTO", "PRODUCTO_MEDICO", "UTENSILIO").contains(tipo)) return "El tipo de producto no es válido.";
        if ("MEDICAMENTO".equals(tipo) && valor(fila, "registroInvima").isBlank()) return "El registro Invima es obligatorio para medicamentos.";
        if (!fechaValida(valor(fila, "fechaCompra"))) return "La fecha de compra debe usar formato AAAA-MM-DD.";
        if (!fechaValidaOpcional(valor(fila, "fechaVence"))) return "La fecha de vencimiento debe usar formato AAAA-MM-DD.";
        if ("MEDICAMENTO".equals(tipo) && valor(fila, "fechaVence").isBlank()) return "La fecha de vencimiento es obligatoria para medicamentos.";
        if (!fechaValidaOpcional(valor(fila, "fechaProximoPedido"))) return "La fecha de próximo pedido debe usar formato AAAA-MM-DD.";
        if (fechaValidaOpcional(valor(fila, "fechaVence")) && !valor(fila, "fechaVence").isBlank()
                && LocalDate.parse(valor(fila, "fechaVence")).isBefore(LocalDate.parse(valor(fila, "fechaCompra")))) {
            return "La fecha de vencimiento no puede ser anterior a la fecha de compra.";
        }
        if (!decimalValido(valor(fila, "precioCompra")) || !decimalValido(valor(fila, "precioVenta"))) return "Los precios deben ser números no negativos.";
        if (!enteroValido(valor(fila, "cantidadActual")) || !enteroValido(valor(fila, "stockMinimo"))) return "Las cantidades deben ser enteros no negativos.";
        if (Integer.parseInt(valor(fila, "cantidadActual")) > 0) {
            if (valor(fila, "fechaVence").isBlank()) return "La fecha de vencimiento es obligatoria cuando se registran existencias.";
            if (valor(fila, "numeroLote").isBlank() || valor(fila, "numeroLote").length() > 50) return "El número de lote es obligatorio cuando hay existencias.";
            if (!decimalValido(valor(fila, "precioCompraLote"))) return "El precio de compra del lote debe ser un número no negativo.";
        }
        fila.put("codigo", codigo);
        fila.put("tipoProducto", tipo);
        return null;
    }

    /** Normaliza el nombre de una columna para admitir diferencias de mayúsculas. */
    private String normalizarEncabezado(String encabezado) {
        return encabezado == null ? "" : encabezado.replace("\uFEFF", "").trim();
    }

    /** Obtiene el valor textual de una fila sin nulos. */
    private String valor(Map<String, String> fila, String campo) {
        return fila.getOrDefault(campo, "").trim();
    }

    /** Valida una fecha obligatoria en formato ISO. */
    private boolean fechaValida(String valor) {
        try { LocalDate.parse(valor); return true; } catch (DateTimeParseException | NullPointerException exception) { return false; }
    }

    /** Valida una fecha ISO opcional. */
    private boolean fechaValidaOpcional(String valor) {
        return valor == null || valor.isBlank() || fechaValida(valor);
    }

    /** Valida un decimal no negativo. */
    private boolean decimalValido(String valor) {
        try { return new java.math.BigDecimal(valor).signum() >= 0; } catch (RuntimeException exception) { return false; }
    }

    /** Valida un entero no negativo. */
    private boolean enteroValido(String valor) {
        try { return Integer.parseInt(valor) >= 0; } catch (RuntimeException exception) { return false; }
    }

    /** Resuelve un usuario de inventario habilitado y autorizado. */
    private Usuario validarUsuario(String usuarioPrincipal) {
        Long usuarioId;
        try { usuarioId = Long.valueOf(usuarioPrincipal); }
        catch (RuntimeException exception) { throw new BadRequestException("La identidad no contiene un id de usuario válido."); }
        Usuario usuario = usuarioRepository.findOneById(usuarioId)
                .orElseThrow(() -> new BadRequestException("El usuario responsable no existe."));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getPerfil() == null
                || !Boolean.TRUE.equals(usuario.getPerfil().getActivo())
                || "DESPACHO_MEDICO".equalsIgnoreCase(usuario.getPerfil().getNombre().replace(' ', '_'))) {
            throw new BadRequestException("El usuario no tiene permisos para cargar inventario.");
        }
        return usuario;
    }

    /** Convierte los datos a JSON para conservar el contenido entre previsualización y confirmación. */
    private String serializar(Object valor) {
        if (!(valor instanceof List<?> elementos)) {
            throw new BadRequestException("No fue posible guardar la previsualización de la carga.");
        }
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             DataOutputStream salida = new DataOutputStream(bytes)) {
            salida.writeInt(elementos.size());
            for (Object elemento : elementos) {
                if (!(elemento instanceof Map<?, ?> mapa)) {
                    throw new BadRequestException("El contenido de la previsualización no es válido.");
                }
                salida.writeInt(mapa.size());
                for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
                    escribirTexto(salida, String.valueOf(entrada.getKey()));
                    escribirTexto(salida, String.valueOf(entrada.getValue()));
                }
            }
            return "\"B64:" + Base64.getEncoder().encodeToString(bytes.toByteArray()) + "\"";
        } catch (Exception exception) {
            throw new BadRequestException("No fue posible guardar la previsualización de la carga.");
        }
    }

    /** Recupera las filas válidas almacenadas para su confirmación. */
    private List<Map<String, String>> deserializarRegistros(String json) {
        return deserializarMapas(json).stream()
                .filter(fila -> "REGISTRO".equals(fila.get("_tipo")))
                .peek(fila -> fila.remove("_tipo"))
                .toList();
    }

    /** Recupera errores detectados durante la prevalidación. */
    private List<Map<String, Object>> deserializarErrores(String json) {
        List<Map<String, String>> filas = deserializarMapas(json);
        List<Map<String, Object>> errores = new ArrayList<>();
        for (Map<String, String> fila : filas) {
            if (fila.containsKey("_tipo") && !"ERROR".equals(fila.get("_tipo"))) {
                continue;
            }
            Map<String, Object> error = new LinkedHashMap<>();
            if (fila.containsKey("fila")) error.put("fila", Integer.parseInt(fila.get("fila")));
            if (fila.containsKey("motivo")) error.put("motivo", fila.get("motivo"));
            errores.add(error);
        }
        return errores;
    }

    /** Decodifica mapas persistidos en el campo JSON de la carga. */
    private List<Map<String, String>> deserializarMapas(String json) {
        try {
            String contenido = json.substring(1, json.length() - 1);
            if (!contenido.startsWith("B64:")) throw new IllegalArgumentException("Formato inválido");
            byte[] bytes = Base64.getDecoder().decode(contenido.substring(4));
            try (DataInputStream entrada = new DataInputStream(new ByteArrayInputStream(bytes))) {
                int cantidad = entrada.readInt();
                if (cantidad < 0 || cantidad > 100_000) throw new IllegalArgumentException("Tamaño inválido");
                List<Map<String, String>> mapas = new ArrayList<>();
                for (int i = 0; i < cantidad; i++) {
                    int campos = entrada.readInt();
                    if (campos < 0 || campos > 1000) throw new IllegalArgumentException("Fila inválida");
                    Map<String, String> mapa = new LinkedHashMap<>();
                    for (int campo = 0; campo < campos; campo++) {
                        mapa.put(leerTexto(entrada), leerTexto(entrada));
                    }
                    mapas.add(mapa);
                }
                if (entrada.available() != 0) throw new IllegalArgumentException("Datos adicionales");
                return mapas;
            }
        } catch (Exception exception) {
            throw new BadRequestException("No fue posible recuperar los datos de la carga masiva.");
        }
    }

    /** Escribe texto UTF-8 con longitud explícita para conservar celdas extensas. */
    private void escribirTexto(DataOutputStream salida, String valor) throws Exception {
        byte[] bytes = valor.getBytes(StandardCharsets.UTF_8);
        salida.writeInt(bytes.length);
        salida.write(bytes);
    }

    /** Lee texto UTF-8 guardado con longitud explícita. */
    private String leerTexto(DataInputStream entrada) throws Exception {
        int longitud = entrada.readInt();
        if (longitud < 0 || longitud > entrada.available()) throw new IllegalArgumentException("Texto inválido");
        return new String(entrada.readNBytes(longitud), StandardCharsets.UTF_8);
    }

    /** Devuelve un error breve sin exponer detalles internos del servidor. */
    private String mensajeSeguro(RuntimeException exception) {
        return exception instanceof BadRequestException && exception.getMessage() != null
                ? exception.getMessage() : "El registro no pudo importarse por una restricción del inventario.";
    }
}
