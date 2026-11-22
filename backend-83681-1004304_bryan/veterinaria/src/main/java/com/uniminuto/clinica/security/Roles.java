package com.uniminuto.clinica.security;

import java.text.Normalizer;
import java.util.Locale;

/** Perfiles del sistema (tabla perfil) normalizados: sin tildes, mayúsculas y con guion bajo. */
public final class Roles {

    public static final String ADMIN = "ADMINISTRADOR";
    public static final String RECEPCION = "RECEPCION";
    public static final String MEDICO = "MEDICO";
    public static final String DESPACHO = "DESPACHO_MEDICO";

    private Roles() {
    }

    public static String normalize(String perfilNombre) {
        if (perfilNombre == null) {
            return "";
        }
        String s = Normalizer.normalize(perfilNombre.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return s.toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "_");
    }
}
