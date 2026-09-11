package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioApiController implements UsuarioApi {
    @Autowired
    private UsuarioService usuarioService;

    @Override
    public ResponseEntity<List<Usuario>> listarUsuarios() throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.listarUsuarios());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarUsuario(@RequestBody UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.usuarioService.guardarUsuario(usuarioRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarUsuario(@RequestBody UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.actualizarUsuario(usuarioRq));
    }
}
