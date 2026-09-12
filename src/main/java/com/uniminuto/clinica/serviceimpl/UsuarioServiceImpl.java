package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<UsuarioRS> listarUsuarios() throws BadRequestException {
        return usuarioRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::aPublico)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioRS crearUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        this.validarUsuario(usuarioRq, true);

        String email = usuarioRq.getEmail().trim().toLowerCase();
        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(email);
        if (optUsuario.isPresent()) {
            throw new BadRequestException("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioRq.getUsername().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(this.cifrarMD5(usuarioRq.getPasswordHash()));
        usuario.setRol(usuarioRq.getRol() == null || usuarioRq.getRol().trim().isEmpty()
                ? "usuario" : usuarioRq.getRol().trim());
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now()); // Esta sí existe en tu BD

        usuarioRepository.save(usuario);
        return aPublico(usuario);
    }

    @Override
    public UsuarioRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        if (usuarioRq == null || usuarioRq.getId() == null) {
            throw new BadRequestException("El ID del usuario es obligatorio para actualizar");
        }

        Optional<Usuario> optUsuario = this.usuarioRepository.findById(usuarioRq.getId());
        if (optUsuario.isEmpty()) {
            throw new BadRequestException("Usuario no encontrado");
        }

        Usuario usuario = optUsuario.get();
        this.validarUsuario(usuarioRq, false);

        if (usuarioRq.getUsername() != null && !usuarioRq.getUsername().trim().isEmpty()) {
            usuario.setUsername(usuarioRq.getUsername().trim());
        }

        if (usuarioRq.getEmail() != null && !usuarioRq.getEmail().trim().isEmpty()) {
            String emailNuevo = usuarioRq.getEmail().trim().toLowerCase();
            Optional<Usuario> optDuplicado = this.usuarioRepository.findByEmail(emailNuevo);
            if (optDuplicado.isPresent() && !optDuplicado.get().getUsuarioId().equals(usuario.getUsuarioId())) {
                throw new BadRequestException("Ya existe otro usuario con ese email");
            }
            usuario.setEmail(emailNuevo);
        }

        if (usuarioRq.getRol() != null && !usuarioRq.getRol().trim().isEmpty()) {
            usuario.setRol(usuarioRq.getRol().trim());
        }

        if (usuarioRq.getPasswordHash() != null && !usuarioRq.getPasswordHash().trim().isEmpty()) {
            usuario.setPasswordHash(this.cifrarMD5(usuarioRq.getPasswordHash()));
        }

        usuarioRepository.save(usuario);

        return aPublico(usuario);
    }

    private void validarUsuario(UsuarioRq usuarioRq, boolean esCreacion) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        if (esCreacion || usuarioRq.getUsername() != null) {
            if (usuarioRq.getUsername() == null || usuarioRq.getUsername().trim().isEmpty()) {
                throw new BadRequestException("El username es obligatorio");
            }
            if (usuarioRq.getUsername().trim().length() < 3) {
                throw new BadRequestException("El username debe tener al menos 3 caracteres");
            }
        }

        if (esCreacion || usuarioRq.getEmail() != null) {
            if (usuarioRq.getEmail() == null || usuarioRq.getEmail().trim().isEmpty()) {
                throw new BadRequestException("El email es obligatorio");
            }
            if (!EMAIL_PATTERN.matcher(usuarioRq.getEmail().trim()).matches()) {
                throw new BadRequestException("El email no tiene un formato válido");
            }
        }

        if (esCreacion || usuarioRq.getPasswordHash() != null) {
            if (usuarioRq.getPasswordHash() == null || usuarioRq.getPasswordHash().trim().isEmpty()) {
                if (esCreacion) {
                    throw new BadRequestException("El password es obligatorio");
                }
            } else if (usuarioRq.getPasswordHash().length() < 4) {
                throw new BadRequestException("El password debe tener al menos 4 caracteres");
            }
        }
    }

    private String cifrarMD5(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo cifrar el password", e);
        }
    }

    private UsuarioRS aPublico(Usuario usuario) {
        UsuarioRS rs = new UsuarioRS();
        rs.setUsuarioId(usuario.getUsuarioId());
        rs.setUsername(usuario.getUsername());
        rs.setEmail(usuario.getEmail());
        rs.setRol(usuario.getRol());
        rs.setFechaCreacion(usuario.getFechaCreacion());
        return rs;
    }
}