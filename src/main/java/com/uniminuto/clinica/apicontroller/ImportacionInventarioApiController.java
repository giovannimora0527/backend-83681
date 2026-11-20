package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.api.ImportacionInventarioApi;
import com.uniminuto.clinica.models.ImportacionConfirmRs;
import com.uniminuto.clinica.models.ImportacionPreviewRs;
import com.uniminuto.clinica.service.ImportacionInventarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@RestController
public class ImportacionInventarioApiController implements ImportacionInventarioApi {
    private final ImportacionInventarioService service;
    public ImportacionInventarioApiController(ImportacionInventarioService service) { this.service = service; }
    @Override public ResponseEntity<ImportacionPreviewRs> prevalidar(MultipartFile archivo, HttpServletRequest request) {
        return ResponseEntity.ok(service.prevalidar(archivo, usuarioId(request)));
    }
    @Override public ResponseEntity<ImportacionConfirmRs> confirmar(Long id, HttpServletRequest request) {
        return ResponseEntity.ok(service.confirmar(id, usuarioId(request)));
    }
    private static Long usuarioId(HttpServletRequest request) { return Long.valueOf(request.getAttribute("authenticatedUserId").toString()); }
}
