package com.uniminuto.clinica.api;

import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/** API para previsualizar y confirmar cargas masivas de inventario. */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/inventario/carga-masiva")
public interface CargaMasivaInventarioApi {

    /** Valida un archivo CSV/XLSX y presenta registros válidos y errores por fila. */
    @PostMapping(value = "/previsualizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> previsualizar(@RequestPart("archivo") MultipartFile archivo,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;

    /** Confirma la importación de los registros válidos de una previsualización. */
    @PostMapping(value = "/{cargaId}/confirmar", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Map<String, Object>> confirmar(@PathVariable Long cargaId,
            @AuthenticationPrincipal String usuarioResponsableId) throws BadRequestException;
}
