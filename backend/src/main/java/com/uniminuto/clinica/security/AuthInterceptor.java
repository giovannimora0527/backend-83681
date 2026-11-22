package com.uniminuto.clinica.security;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.support.Db;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Autenticación y autorización en el backend:
 * 1) exige un JWT válido (firma + expiración) y una sesión vigente en la tabla session;
 * 2) valida el rol contra la matriz {@link Policy}; si no tiene permiso responde 403.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR = "authUser";

    private final JwtService jwtService;
    private final Db db;

    public AuthInterceptor(JwtService jwtService, Db db) {
        this.jwtService = jwtService;
        this.db = db;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (isPublic(path)) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ") || header.length() < 20) {
            throw new BadRequestException("Debe iniciar sesión para usar este servicio.", HttpStatus.UNAUTHORIZED);
        }
        String token = header.substring(7).trim();
        AuthUser user = jwtService.parse(token);

        long vigente = db.count("SELECT COUNT(*) FROM session WHERE token = ? AND fecha_expiracion > NOW()", token);
        if (vigente == 0) {
            throw new BadRequestException("La sesión no está activa. Inicie sesión nuevamente.",
                    HttpStatus.UNAUTHORIZED);
        }
        if (!Policy.allowed(request.getMethod(), path, user.rol())) {
            throw new BadRequestException("Su perfil (" + user.rol() + ") no tiene permiso para esta operación.",
                    HttpStatus.FORBIDDEN);
        }
        request.setAttribute(ATTR, user);
        return true;
    }

    private static boolean isPublic(String path) {
        return path.startsWith("/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/error");
    }
}
