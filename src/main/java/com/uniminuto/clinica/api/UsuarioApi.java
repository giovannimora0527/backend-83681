package com.uniminuto.clinica.api;

import com.uniminuto.clinica.models.UsuarioRequestDTO;
import com.uniminuto.clinica.models.UsuarioResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UsuarioApi {
    ResponseEntity<List<UsuarioResponseDTO>> listar();
    ResponseEntity<UsuarioResponseDTO> guardar(UsuarioRequestDTO dto);
    ResponseEntity<UsuarioResponseDTO> actualizar(UsuarioRequestDTO dto);
}
