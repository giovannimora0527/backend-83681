package com.uniminuto.clinica.service;
import com.uniminuto.clinica.models.ImportacionConfirmRs;
import com.uniminuto.clinica.models.ImportacionPreviewRs;
import org.springframework.web.multipart.MultipartFile;
public interface ImportacionInventarioService {
    ImportacionPreviewRs prevalidar(MultipartFile file, Long usuarioId);
    ImportacionConfirmRs confirmar(Long id, Long usuarioId);
}
