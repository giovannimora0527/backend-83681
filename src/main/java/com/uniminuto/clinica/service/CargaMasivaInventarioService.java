package com.uniminuto.clinica.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/** Define el flujo de previsualización y confirmación de cargas de inventario. */
public interface CargaMasivaInventarioService {

    /** Valida un CSV/XLSX y conserva los registros válidos para confirmación posterior. */
    Map<String, Object> previsualizar(MultipartFile archivo, String usuarioResponsableId);

    /** Importa los registros válidos de una previsualización confirmada por su responsable. */
    Map<String, Object> confirmar(Long cargaId, String usuarioResponsableId);
}
