package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Usuario crearUsuario(Usuario usuario) {
        if (usuario.getPasswordHash() != null) {
            String md5Hex = DigestUtils.md5DigestAsHex(usuario.getPasswordHash().getBytes());
            usuario.setPasswordHash(md5Hex);
        }

        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizarUsuario(Usuario usuario) {
        return null;
    }

    @Override
    public List<Usuario> getlistarUsuarios() {
        return List.of();
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    @Override
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado con ID: " + id));

        usuarioExistente.setUsername(usuarioActualizado.getUsername());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setRol(usuarioActualizado.getRol());
        usuarioExistente.setActivo(usuarioActualizado.getActivo());

        if (usuarioActualizado.getPasswordHash() != null && !usuarioActualizado.getPasswordHash().isEmpty()) {
            String md5Hex = DigestUtils.md5DigestAsHex(usuarioActualizado.getPasswordHash().getBytes());
            usuarioExistente.setPasswordHash(md5Hex);
        }

        return usuarioRepository.save(usuarioExistente);
    }
}