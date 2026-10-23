package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioApiController implements UsuarioApi {

    private final UsuarioService usuarioService;

    public UsuarioApiController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public ResponseEntity<List<UsuarioRs>> getUsuarios() throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.obtenerUsuariosOrdenados());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.crearUsuario(usuarioRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.actualizarUsuario(usuarioRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> eliminar(Long id) {
        return ResponseEntity.ok(usuarioService.eliminarUsuario(id));
    }
}
