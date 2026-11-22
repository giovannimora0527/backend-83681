package com.uniminuto.clinica.security;

import java.util.List;
import java.util.Set;

import static com.uniminuto.clinica.security.Roles.ADMIN;
import static com.uniminuto.clinica.security.Roles.DESPACHO;
import static com.uniminuto.clinica.security.Roles.MEDICO;
import static com.uniminuto.clinica.security.Roles.RECEPCION;

/**
 * Matriz de permisos aplicada en el BACKEND (no solo ocultando menús en el frontend).
 * Se evalúa la regla con el prefijo más largo que coincida con la ruta; si ninguna
 * coincide, la petición se rechaza (denegar por defecto).
 */
public final class Policy {

    /** Regla: prefijo de ruta, roles que pueden leer (GET) y roles que pueden escribir (POST/PUT/DELETE/PATCH). */
    public record Rule(String prefix, Set<String> read, Set<String> write) {
    }

    private static final Set<String> NONE = Set.of();

    public static final List<Rule> RULES = List.of(
            // ----- módulos ya existentes -----
            new Rule("/usuario", Set.of(ADMIN), Set.of(ADMIN)),
            new Rule("/medico", Set.of(ADMIN, RECEPCION, MEDICO), Set.of(ADMIN)),
            new Rule("/cliente", Set.of(ADMIN, RECEPCION, MEDICO), Set.of(ADMIN, RECEPCION)),
            new Rule("/mascota", Set.of(ADMIN, RECEPCION, MEDICO), Set.of(ADMIN, RECEPCION)),
            new Rule("/raza", Set.of(ADMIN, RECEPCION, MEDICO), Set.of(ADMIN)),
            new Rule("/especializacion", Set.of(ADMIN, RECEPCION, MEDICO), Set.of(ADMIN)),
            new Rule("/cita", Set.of(ADMIN, RECEPCION), Set.of(ADMIN, RECEPCION)),
            new Rule("/historia-clinica", Set.of(ADMIN, MEDICO), Set.of(ADMIN, MEDICO)),
            new Rule("/anotacion-historia", Set.of(ADMIN, MEDICO), Set.of(ADMIN, MEDICO)),
            new Rule("/medicamento", Set.of(ADMIN, RECEPCION, MEDICO, DESPACHO), Set.of(ADMIN)),
            new Rule("/email", Set.of(ADMIN), Set.of(ADMIN)),
            new Rule("/clinica", Set.of(ADMIN), Set.of(ADMIN)),
            // ----- módulo 1: inventario -----
            new Rule("/inventario", Set.of(ADMIN, RECEPCION, MEDICO, DESPACHO), Set.of(ADMIN)),
            new Rule("/inventario/carga", Set.of(ADMIN), Set.of(ADMIN)),
            // ----- módulo 2: calendario del médico -----
            new Rule("/citas-medicas", Set.of(ADMIN, MEDICO), Set.of(MEDICO)),
            // ----- módulo 3: atención, fórmulas, catálogo, facturación -----
            new Rule("/atencion", Set.of(ADMIN, MEDICO), Set.of(MEDICO)),
            new Rule("/formulas", Set.of(ADMIN, MEDICO), Set.of(MEDICO)),
            new Rule("/catalogo", Set.of(ADMIN, RECEPCION, MEDICO), Set.of(ADMIN)),
            new Rule("/facturas", Set.of(ADMIN, RECEPCION), Set.of(ADMIN, RECEPCION)),
            // ----- módulo 4: reportes -----
            new Rule("/reportes", Set.of(ADMIN), NONE),
            // ----- módulo 5: despacho (exclusivo del perfil DESPACHO_MEDICO) -----
            new Rule("/despacho", Set.of(DESPACHO), Set.of(DESPACHO))
    );

    private Policy() {
    }

    /** @return la regla aplicable a la ruta, o null si no hay ninguna. */
    public static Rule find(String path) {
        Rule best = null;
        for (Rule r : RULES) {
            boolean match = path.equals(r.prefix()) || path.startsWith(r.prefix() + "/");
            if (match && (best == null || r.prefix().length() > best.prefix().length())) {
                best = r;
            }
        }
        return best;
    }

    public static boolean allowed(String method, String path, String rol) {
        Rule r = find(path);
        if (r == null) {
            return false;
        }
        boolean read = "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
        return (read ? r.read() : r.write()).contains(rol);
    }
}
