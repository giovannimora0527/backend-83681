package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.EspecializacionApi;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
=======
import com.uniminuto.clinica.models.EspecializacionRq;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
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
<<<<<<< HEAD
    public ResponseEntity<MiRespuestaRS> crearEspecializacion(Especializacion especializacion) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.crearEspecializacion(especializacion));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarEspecializacion(Especializacion especializacion) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.actualizarEspecializacion(especializacion));
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    }
}
