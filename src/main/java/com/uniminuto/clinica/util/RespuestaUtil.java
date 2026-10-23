package com.uniminuto.clinica.util;

import com.uniminuto.clinica.models.MiRespuestaRS;

/**
 * Utilidad para construir las respuestas de exito (MiRespuestaRS)
 * sin repetir el mismo codigo en cada servicio.
 */
public final class RespuestaUtil {

    /**
     * Constructor privado: esta clase solo tiene metodos estaticos.
     */
    private RespuestaUtil() {
    }

    /**
     * Construye una respuesta de exito.
     *
     * @param status codigo HTTP (200 actualizado, 201 creado).
     * @param mensaje mensaje para mostrar al usuario.
     * @return respuesta con status y mensaje.
     */
    public static MiRespuestaRS crear(int status, String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(status);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
