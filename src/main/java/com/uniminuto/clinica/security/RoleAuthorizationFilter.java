package com.uniminuto.clinica.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Set;

/** Verifica el JWT existente y aplica el perfil en el servidor para cada ruta REST. */
@Component
public class RoleAuthorizationFilter extends OncePerRequestFilter {
    private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "RECEPCION", "MEDICO", "DESPACHO_MEDICO");
    private final String secret;

    public RoleAuthorizationFilter(@Value("${app.jwt.secret:}") String secret) { this.secret = secret; }

    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || path.startsWith("/auth/")
                || path.equals("/clinica/test") || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs");
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain chain) throws ServletException, IOException {
        if (secret.isBlank()) { reject(response, 503, "La autenticación no está configurada."); return; }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            reject(response, 401, "Se requiere un token Bearer válido."); return;
        }
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(authorization.substring(7)).getPayload();
            String role = claims.get("rol", String.class);
            String normalizedRole = role == null ? "" : role.trim().toUpperCase();
            if (!ROLES.contains(normalizedRole)) { reject(response, 403, "El perfil no tiene acceso a esta API."); return; }
            request.setAttribute("authenticatedUserId", claims.getSubject());
            request.setAttribute("authenticatedRole", normalizedRole);
            if (!authorized(request, normalizedRole)) { reject(response, 403, "El perfil no tiene permiso para esta operación."); return; }
            chain.doFilter(request, response);
        } catch (Exception e) {
            reject(response, 401, "El token es inválido o expiró.");
        }
    }

    private static boolean authorized(HttpServletRequest request, String role) {
        String path = request.getServletPath();
        String method = request.getMethod();
        if (path.matches("/despacho/[0-9]+/revertir")) return role.equals("ADMINISTRADOR");
        if (path.startsWith("/despacho/")) return role.equals("DESPACHO_MEDICO");
        if (path.startsWith("/facturas/")) {
            if (path.endsWith("/anular")) return role.equals("ADMINISTRADOR");
            return role.equals("ADMINISTRADOR") || role.equals("RECEPCION");
        }
        if (role.equals("DESPACHO_MEDICO")) {
            return (path.startsWith("/inventario/") || path.equals("/medicamento/listar")
                    || path.matches("/medicamento/[0-9]+") || path.startsWith("/medicamento/stock-")
                    || path.startsWith("/medicamento/agotados") || path.startsWith("/medicamento/por-vencer")
                    || path.startsWith("/medicamento/vencidos"))
                    && (method.equals("GET") || method.equals("HEAD"));
        }
        if (path.startsWith("/inventario/") && !(method.equals("GET") || method.equals("HEAD")))
            return role.equals("ADMINISTRADOR");
        if (path.startsWith("/especializacion/") || path.startsWith("/tarifas/"))
            return (method.equals("GET") || method.equals("HEAD"))
                    ? role.equals("ADMINISTRADOR") || role.equals("RECEPCION") || role.equals("MEDICO")
                    : role.equals("ADMINISTRADOR");
        if (path.startsWith("/usuario/") || path.startsWith("/medico/crear") || path.startsWith("/medico/actualizar")
                || path.startsWith("/reportes/"))
            return role.equals("ADMINISTRADOR");
        if (path.startsWith("/cita/")) {
            if (path.startsWith("/cita/disponibilidad/")) return role.equals("ADMINISTRADOR") || role.equals("RECEPCION")
                    || (role.equals("MEDICO") && method.equals("GET"));
            if (role.equals("MEDICO")) return path.equals("/cita/mis-citas")
                    || path.equals("/cita/crear") || path.equals("/cita/actualizar");
            return role.equals("ADMINISTRADOR") || role.equals("RECEPCION");
        }
        if (path.startsWith("/atenciones/")) return role.equals("MEDICO") || role.equals("ADMINISTRADOR");
        if (path.startsWith("/historia-clinica/") || path.startsWith("/formula-medica/"))
            return role.equals("ADMINISTRADOR") || role.equals("RECEPCION") || role.equals("MEDICO");
        return role.equals("ADMINISTRADOR") || role.equals("RECEPCION") || role.equals("MEDICO");
    }

    private static void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + message + "\"}");
    }
}
