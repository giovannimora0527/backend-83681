package com.uniminuto.clinica.apicontroller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta de las tablas de la base de datos "clinica" para los modulos de gestion
 * del frontend.
 *
 * GET /clinica/v1/tablas/{tabla}  ->  SELECT * FROM {tabla}
 *
 * Devuelve todos los registros actuales de la tabla, asi que lo que se agregue en la BD
 * aparece en la siguiente consulta. Los nombres de columna se envian en camelCase
 * (fecha_hora -> fechaHora) para que coincidan con los modelos de Angular.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/tablas")
public class TablasApiController {

    // Solo se permiten estas tablas (evita inyeccion SQL y exponer datos sensibles).
    private static final Set<String> TABLAS = Set.of(
            "usuario", "cliente", "mascota", "raza", "medico", "especializacion",
            "cita", "historia_medica", "anotacion_historia", "formula_medica",
            "medicamento");  // "session" excluida: contiene tokens de acceso

    // Columnas que no se envian al frontend.
    private static final Set<String> COLUMNAS_OCULTAS = Set.of("password_hash");

    private final JdbcTemplate jdbcTemplate;

    public TablasApiController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping(value = "/{tabla}", produces = {"application/json"})
    public ResponseEntity<List<Map<String, Object>>> listarTabla(@PathVariable String tabla) {
        if (!TABLAS.contains(tabla)) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> filas = jdbcTemplate.queryForList("SELECT * FROM `" + tabla + "`");
        List<Map<String, Object>> respuesta = new ArrayList<>();

        for (Map<String, Object> fila : filas) {
            Map<String, Object> registro = new LinkedHashMap<>();
            for (Map.Entry<String, Object> columna : fila.entrySet()) {
                String nombre = columna.getKey().toLowerCase();
                if (COLUMNAS_OCULTAS.contains(nombre)) {
                    continue;
                }
                Object valor = columna.getValue();
                // El token de sesion se recorta por seguridad.
                if ("token".equals(nombre) && valor != null) {
                    String token = valor.toString();
                    valor = token.length() > 20 ? token.substring(0, 20) + "..." : token;
                }
                registro.put(aCamelCase(nombre), valor);
            }
            respuesta.add(registro);
        }
        return ResponseEntity.ok(respuesta);
    }

    // fecha_creacion_registro -> fechaCreacionRegistro
    private String aCamelCase(String columna) {
        StringBuilder sb = new StringBuilder();
        boolean mayuscula = false;
        for (char c : columna.toCharArray()) {
            if (c == '_') {
                mayuscula = true;
            } else {
                sb.append(mayuscula ? Character.toUpperCase(c) : c);
                mayuscula = false;
            }
        }
        return sb.toString();
    }
}
