package com.uniminuto.clinica.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String cifrar(String texto) {

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] hash = md.digest(
                    texto.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder resultado = new StringBuilder();

            for (byte b : hash) {
                resultado.append(String.format("%02x", b));
            }

            return resultado.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al cifrar la contraseña", e);
        }
    }
}
