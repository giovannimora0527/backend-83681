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

/** Recibe las solicitudes de consulta y edición del catálogo de especialidades veterinarias. */
@RestController
public class EspecializacionApiController implements EspecializacionApi {

    @Autowired
    private EspecializacionService especializacionService;

    /** Consulta las especialidades veterinarias. */
    @Override
    public ResponseEntity<List<Especializacion>> listarEspecializaciones() throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.listarEspecializaciones());
    }

    /** Busca las especialidades veterinarias. */
    @Override
    public ResponseEntity<List<Especializacion>> buscarEspecializaciones(String termino) {
        return ResponseEntity.ok(this.especializacionService.buscarEspecializaciones(termino));
    }

    /** Registra la especialidad veterinaria. */
    @Override
    public ResponseEntity<MiRespuestaRS> crearEspecializacion(EspecializacionRq especializacion) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.crearEspecializacion(especializacion));
    }

    /** Actualiza la especialidad veterinaria. */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarEspecializacion(EspecializacionRq especializacion) throws BadRequestException {
        return ResponseEntity.ok(this.especializacionService.actualizarEspecializacion(especializacion));
    }
}
