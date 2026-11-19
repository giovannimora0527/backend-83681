package com.uniminuto.clinica.service;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.TestRs;
import org.springframework.web.multipart.MultipartFile;

/** Define las operaciones disponibles para gestionar el envío de correos. */
public interface EmailService {

    /** Devuelve una respuesta para comprobar el acceso al servicio. */
    TestRs testService();

    /** Envía un correo de texto con el asunto y el contenido recibidos. */
    void enviarCorreoSimple(String to, String subject, String body)
            throws BadRequestException;

    /** Envía un correo al destinatario indicado. */
    void enviarCorreo(String to, String subject, String body, String from)
            throws BadRequestException;

    /**
     * Obtiene el remitente configurado para el correo.
     * @return Remitente.
     */
    String getTo();

    /** Envía un correo junto con los archivos indicados. */
    void enviarCorreoConAdjuntos(String to, String subject, String body, String from,
                                 MultipartFile adjunto, String filename)
            throws BadRequestException;
}
