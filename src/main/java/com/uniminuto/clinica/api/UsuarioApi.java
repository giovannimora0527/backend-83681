package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.UsuarioRequestDTO;
import com.uniminuto.clinica.models.UsuarioResponseDTO;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface UsuarioApi {
    ResponseEntity<List<UsuarioResponseDTO>> listar();
    ResponseEntity<UsuarioResponseDTO> crear(UsuarioRequestDTO dto);
    ResponseEntity<UsuarioResponseDTO> actualizar(Long id, UsuarioRequestDTO dto);
}