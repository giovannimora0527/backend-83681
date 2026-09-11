package com.uniminuto.clinica.controller;

import com.uniminuto.clinica.api.UsuarioResponse;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // CREAR USUARIO
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(
            @RequestBody Usuario usuario) {

        Usuario usuarioCreado = usuarioService.crear(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UsuarioResponse(usuarioCreado));
    }

    // LISTAR USUARIOS
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {

        List<UsuarioResponse> usuarios = usuarioService.listar()
                .stream()
                .map(UsuarioResponse::new)
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    // ACTUALIZAR USUARIO
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        Usuario usuarioActualizado =
                usuarioService.actualizar(id, usuario);

        return ResponseEntity.ok(
                new UsuarioResponse(usuarioActualizado)
        );
    }
    @GetMapping("/prueba")
    public String prueba() {
        return "El controlador funciona";
    }
}
