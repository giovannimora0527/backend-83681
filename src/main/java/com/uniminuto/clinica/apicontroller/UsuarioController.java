package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.models.UsuarioRequestDTO;
import com.uniminuto.clinica.models.UsuarioResponseDTO;
import com.uniminuto.clinica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Autores: Jose David Aguilar, Juan David Gomez, Wilson Marin Gallego,
 * Jose David Lopez, Nel Felipe Poveda Peña
 */
@RestController
@RequestMapping("/clinica/v1/user")
public class UsuarioController implements UsuarioApi {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    @GetMapping("/listar-usuarios")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @Override
    @PostMapping("/guardar-usuario")
    public ResponseEntity<UsuarioResponseDTO> guardar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.guardarUsuario(dto));
    }

    @Override
    @PutMapping("/actualizar-usuario")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(dto));
    }
}
