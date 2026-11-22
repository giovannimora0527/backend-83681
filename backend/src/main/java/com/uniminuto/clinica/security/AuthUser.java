package com.uniminuto.clinica.security;

/** Usuario autenticado, extraído del JWT validado en cada petición. */
public record AuthUser(Long id, String username, String email, String rol) {

    public boolean is(String... roles) {
        for (String r : roles) {
            if (r.equals(rol)) {
                return true;
            }
        }
        return false;
    }
}
