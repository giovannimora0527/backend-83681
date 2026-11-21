package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.service.ImportacionInventarioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/inventario/importacion")
public class ImportacionInventarioApiController {
    private final ImportacionInventarioService servicio;
    public ImportacionInventarioApiController(ImportacionInventarioService servicio){this.servicio=servicio;}
    @PostMapping(value="/vista-previa",consumes="multipart/form-data")
    public ImportacionInventarioService.VistaPrevia vistaPrevia(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        return servicio.validar(archivo.getBytes());
    }
    @PostMapping(value="/confirmar",consumes="multipart/form-data")
    public ImportacionInventarioService.VistaPrevia confirmar(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        return servicio.confirmar(archivo.getBytes());
    }
}
