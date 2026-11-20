package com.uniminuto.clinica.api;
import com.uniminuto.clinica.models.ImportacionConfirmRs;
import com.uniminuto.clinica.models.ImportacionPreviewRs;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RequestMapping("/inventario/importaciones")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public interface ImportacionInventarioApi {
    @PostMapping(value="/prevalidar", consumes="multipart/form-data", produces="application/json")
    ResponseEntity<ImportacionPreviewRs> prevalidar(@RequestPart("archivo") MultipartFile archivo, HttpServletRequest request);
    @PostMapping(value="/{id}/confirmar", produces="application/json")
    ResponseEntity<ImportacionConfirmRs> confirmar(@PathVariable Long id, HttpServletRequest request);
}
