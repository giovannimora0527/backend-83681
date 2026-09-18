package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.ResourceNotFoundException;
import com.uniminuto.clinica.models.UsuarioRequestDTO;
import com.uniminuto.clinica.models.UsuarioResponseDTO;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAllByOrderByUsernameAsc()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPasswordHash(cifrarMD5(dto.getPassword()));
        usuario.setRol(dto.getRol());
        usuario.setEmail(dto.getEmail());
        usuario.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        usuario.setFechaCreacion(LocalDateTime.now());

        return convertirADTO(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioResponseDTO actualizarUsuario(Long id, @org.jetbrains.annotations.NotNull UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        usuario.setUsername(dto.getUsername());
        usuario.setRol(dto.getRol());
        usuario.setEmail(dto.getEmail());
        if (dto.getActivo() != null) usuario.setActivo(dto.getActivo());

        // Solo re-cifra si mandaron un password nuevo
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPasswordHash(cifrarMD5(dto.getPassword()));
        }

        return convertirADTO(usuarioRepository.save(usuario));
    }

    private String cifrarMD5(String textoPlano) {
        return DigestUtils.md5DigestAsHex(textoPlano.getBytes());
    }

    private UsuarioResponseDTO convertirADTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRol(usuario.getRol());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setActivo(usuario.getActivo());
        dto.setEmail(usuario.getEmail());
        return dto; // nunca se setea el password
    }
}