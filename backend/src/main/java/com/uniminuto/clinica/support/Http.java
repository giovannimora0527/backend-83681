package com.uniminuto.clinica.support;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** Respuestas de archivos descargables (PDF / Excel). */
public final class Http {

    public static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private Http() {
    }

    public static ResponseEntity<byte[]> pdf(byte[] data, String filename) {
        return file(data, filename, MediaType.APPLICATION_PDF);
    }

    public static ResponseEntity<byte[]> xlsx(byte[] data, String filename) {
        return file(data, filename, XLSX);
    }

    private static ResponseEntity<byte[]> file(byte[] data, String filename, MediaType type) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(type);
        h.setContentDisposition(ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build());
        h.setContentLength(data.length);
        return ResponseEntity.ok().headers(h).body(data);
    }
}
