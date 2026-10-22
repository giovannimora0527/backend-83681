package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.RazaApi;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del catálogo de razas.
 */
@RestController
public class RazaApiController implements RazaApi {

    @Autowired
    private RazaService razaService;

    @Override
    public ResponseEntity<List<Raza>> listarRazas() throws BadRequestException {
        return ResponseEntity.ok(this.razaService.listarRazas());
    }

    @Override
    public ResponseEntity<Raza> buscarRazaPorNombre(String nombre) throws BadRequestException {
        return ResponseEntity.ok(this.razaService.buscarRazaPorNombre(nombre));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarRaza(RazaRq razaRq) throws BadRequestException {
        return ResponseEntity.ok(this.razaService.guardarRaza(razaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarRaza(RazaRq razaRq) throws BadRequestException {
        return ResponseEntity.ok(this.razaService.actualizarRaza(razaRq));
    }
}
