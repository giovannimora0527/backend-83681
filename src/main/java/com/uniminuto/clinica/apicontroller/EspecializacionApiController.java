package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del catálogo de especializaciones.
 */
@RestController
public class EspecializacionApiController implements EspecializacionApi {

    @Autowired
    private EspecializacionService especializacionService;

    @Override
    public ResponseEntity<List<Especializacion>> listarEspecializaciones() throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.listarEspecializaciones());
    }

    @Override
    public ResponseEntity<Especializacion> buscarEspecializacionPorCodigo(String codigo) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.buscarEspecializacionPorCodigo(codigo));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.guardarEspecializacion(especializacionRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.actualizarEspecializacion(especializacionRq));
    }
}
