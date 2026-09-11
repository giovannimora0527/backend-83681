package com.uniminuto.clinica.service.impl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.util.MD5Util;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario crear(Usuario usuario) {

        // Cifrar la contraseña antes de guardarla
        usuario.setPasswordHash(
                MD5Util.cifrar(usuario.getPasswordHash())
        );

        // Fecha de creación automática
        usuario.setFechaCreacion(LocalDateTime.now());

        // Si no se especifica, el usuario queda activo
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> listar() {

        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {

        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado")
                );

        usuarioExistente.setUsername(usuario.getUsername());
        usuarioExistente.setRol(usuario.getRol());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setActivo(usuario.getActivo());

        // Si se envía una nueva contraseña,
        // se cifra antes de guardarla
        if (usuario.getPasswordHash() != null
                && !usuario.getPasswordHash().isBlank()) {

            usuarioExistente.setPasswordHash(
                    MD5Util.cifrar(usuario.getPasswordHash())
            );
        }

        return usuarioRepository.save(usuarioExistente);
    }
}
