package com.uniminuto.clinica.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Portero del backend: valida el token JWT y revisa si el rol
 * tiene permiso para la ruta y el metodo HTTP solicitados.
 */
@Component
public class SeguridadInterceptor implements HandlerInterceptor {

    // Nombres de perfil: deben ser IGUALES a los de la tabla perfil.
    private static final String ADMIN = "Administrador";
    private static final String RECEPCION = "Recepcion";
    private static final String MEDICO = "Medico";
    private static final String DESPACHO = "DESPACHO_MEDICO";

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** Una regla: ruta + metodos (vacio = todos) + roles permitidos. */
    private record Regla(String patron, Set<String> metodos, Set<String> roles) { }

    private static Regla regla(String patron, String metodos, String... roles) {
        Set<String> m = metodos.equals("*") ? Set.of() : Set.of(metodos.split(","));
        return new Regla(patron, m, Set.of(roles));
    }

    /**
     * Tabla de permisos. Se revisa de arriba hacia abajo y gana la PRIMERA
     * regla que coincida. Lo que no aparezca aqui: solo Administrador.
     */
    private static final List<Regla> REGLAS = List.of(
            // --- Solo administrador ---
            regla("/usuario/**", "*", ADMIN),
            regla("/email/**", "*", ADMIN),
            regla("/tarifa/**", "*", ADMIN),
            regla("/reportes/**", "*", ADMIN),

            // --- Despacho (la reversion es la unica parte del administrador) ---
            regla("/despacho/reversion/**", "*", ADMIN),
            regla("/despacho/**", "*", DESPACHO),

            // --- Inventario: lectura para roles existentes, cambios solo administrador ---
            regla("/inventario/**", "GET", ADMIN, RECEPCION, MEDICO, DESPACHO),
            regla("/inventario/**", "*", ADMIN),

            // --- Medicamentos: todos leen, solo el administrador modifica ---
            regla("/medicamento/**", "GET", ADMIN, RECEPCION, MEDICO, DESPACHO),
            regla("/medicamento/**", "*", ADMIN),

            // --- Citas, atencion y facturacion ---
            regla("/cita/**", "*", ADMIN, RECEPCION, MEDICO),
            regla("/atencion/**", "*", ADMIN, MEDICO),
            regla("/factura/**", "*", ADMIN, RECEPCION),
            regla("/pago/**", "*", ADMIN, RECEPCION),

            // --- Clientes y mascotas ---
            regla("/cliente/**", "GET", ADMIN, RECEPCION, MEDICO),
            regla("/cliente/**", "*", ADMIN, RECEPCION),
            regla("/mascota/**", "GET", ADMIN, RECEPCION, MEDICO),
            regla("/mascota/**", "*", ADMIN, RECEPCION),

            // --- Catalogos: todos leen, solo el administrador modifica ---
            regla("/raza/**", "GET", ADMIN, RECEPCION, MEDICO),
            regla("/raza/**", "*", ADMIN),
            regla("/especializacion/**", "GET", ADMIN, RECEPCION, MEDICO),
            regla("/especializacion/**", "*", ADMIN),
            regla("/medico/**", "GET", ADMIN, RECEPCION, MEDICO),
            regla("/medico/**", "*", ADMIN),

            // --- Parte clinica: el despacho NO entra ---
            regla("/formula-medica/**", "*", ADMIN, MEDICO),
            regla("/historia-clinica/**", "*", ADMIN, MEDICO),
            regla("/anotacion-historia/**", "*", ADMIN, MEDICO)
    );

    private final SecretKey clave;

    public SeguridadInterceptor(@Value("${JWT_SECRET}") String secret) {
        // Misma clave que usa Utilidades para firmar el token.
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {
        // El navegador envia una "pregunta previa" (OPTIONS) sin token: se deja pasar.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1) Debe venir el token
        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            responder(response, 401, "UNAUTHORIZED",
                    "Debe iniciar sesion (falta el token).");
            return false;
        }

        // 2) El token debe ser autentico y no estar vencido
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(cabecera.substring(7)).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            responder(response, 401, "UNAUTHORIZED", "Token invalido o vencido.");
            return false;
        }

        // 3) El rol del token debe tener permiso para esta ruta
        String rol = claims.get("rol", String.class);
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (rol == null || !permitido(rol, request.getMethod().toUpperCase(), ruta)) {
            responder(response, 403, "FORBIDDEN",
                    "El perfil " + rol + " no tiene permiso para esta operacion.");
            return false;
        }

        // Dejamos disponible quien es el usuario (lo usaremos en inventario y despacho)
        request.setAttribute("usuarioId", Long.valueOf(claims.getSubject()));
        request.setAttribute("rol", rol);
        return true;
    }

    private boolean permitido(String rol, String metodo, String ruta) {
        for (Regla r : REGLAS) {
            boolean rutaCoincide = MATCHER.match(r.patron(), ruta);
            boolean metodoCoincide = r.metodos().isEmpty() || r.metodos().contains(metodo);
            if (rutaCoincide && metodoCoincide) {
                return r.roles().contains(rol);
            }
        }
        return ADMIN.equals(rol);
    }

    private void responder(HttpServletResponse response, int status,
                           String error, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status
                + ",\"error\":\"" + error
                + "\",\"message\":\"" + mensaje + "\"}");
    }
}
