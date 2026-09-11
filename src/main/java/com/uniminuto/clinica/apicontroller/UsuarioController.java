package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.models.UsuarioRequestDTO;
import com.uniminuto.clinica.models.UsuarioResponseDTO;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController implements UsuarioApi {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @Override
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.crearUsuario(dto));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Long id, @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto));
    }
}