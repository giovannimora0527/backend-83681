package com.uniminuto.clinica.util;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
class PdfGeneratorTests {
    @Test void generaDocumentoPdfLegible(){
        byte[] pdf=PdfGenerator.crear("Atención",List.of("Diagnóstico: revisión general"));
        assertTrue(new String(pdf,StandardCharsets.ISO_8859_1).startsWith("%PDF-"));
    }
}
