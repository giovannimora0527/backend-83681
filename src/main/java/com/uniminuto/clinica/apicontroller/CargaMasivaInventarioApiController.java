package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CargaMasivaInventarioApi;
import com.uniminuto.clinica.service.CargaMasivaInventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/** Atiende el flujo HTTP de importación de medicamentos y productos médicos. */
@RestController
public class CargaMasivaInventarioApiController implements CargaMasivaInventarioApi {

    private final CargaMasivaInventarioService cargaService;

    /** Construye el controlador con el servicio de carga masiva. */
    public CargaMasivaInventarioApiController(CargaMasivaInventarioService cargaService) {
        this.cargaService = cargaService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> previsualizar(MultipartFile archivo, String usuarioResponsableId) {
        return ResponseEntity.ok(cargaService.previsualizar(archivo, usuarioResponsableId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> confirmar(Long cargaId, String usuarioResponsableId) {
        return ResponseEntity.ok(cargaService.confirmar(cargaId, usuarioResponsableId));
    }
}
