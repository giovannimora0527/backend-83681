package com.uniminuto.clinica.security;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.support.Db;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

/** Acceso al usuario autenticado de la petición en curso. */
@Component
public class CurrentUser {

    private final Db db;

    public CurrentUser(Db db) {
        this.db = db;
    }

    public AuthUser get() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        Object u = ra == null ? null : ra.getAttribute(AuthInterceptor.ATTR, RequestAttributes.SCOPE_REQUEST);
        if (u instanceof AuthUser au) {
            return au;
        }
        throw new BadRequestException("Debe iniciar sesión para usar este servicio.", HttpStatus.UNAUTHORIZED);
    }

    /** Id del veterinario asociado al usuario autenticado, o null si el usuario no es veterinario. */
    public Long medicoId() {
        Map<String, Object> m = db.one("SELECT id FROM medico WHERE usuario_id = ?", get().id());
        return m == null ? null : ((Number) m.get("id")).longValue();
    }

    public void requireRole(String... roles) {
        if (!get().is(roles)) {
            throw new BadRequestException("No tiene permiso para realizar esta operación.", HttpStatus.FORBIDDEN);
        }
    }
}
