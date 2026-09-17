package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.UsuarioRequestDTO;
import com.uniminuto.clinica.models.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {
    List<UsuarioResponseDTO> listarUsuarios();
    UsuarioResponseDTO guardarUsuario(UsuarioRequestDTO dto);
    UsuarioResponseDTO actualizarUsuario(UsuarioRequestDTO dto);
}
