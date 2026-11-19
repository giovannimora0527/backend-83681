package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.RazaApi;
import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expone las operaciones HTTP relacionadas con una raza del catálogo. */
@RestController
public class RazaApiController implements RazaApi {

    @Autowired
    private RazaService razaService;

    /** Guarda la raza. */
    @Override
    public ResponseEntity<MiRespuestaRS> guardarRaza(RazaRq razaRq) throws BadRequestException {
        return ResponseEntity.ok(this.razaService.crearRazaNueva(razaRq));
    }

    /** Actualiza la raza. */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarRaza(RazaRq razaRq) throws BadRequestException {
        return ResponseEntity.ok(this.razaService.actualizarRaza(razaRq));
    }

    /** Consulta las razas. */
    @Override
    public ResponseEntity<List<Raza>> listarRazas() throws BadRequestException {
        return ResponseEntity.ok(this.razaService.listarRazas());
    }

    /** Busca las razas. */
    @Override
    public ResponseEntity<List<Raza>> buscarRazas(String termino) {
        return ResponseEntity.ok(this.razaService.buscarRazas(termino));
    }
}
