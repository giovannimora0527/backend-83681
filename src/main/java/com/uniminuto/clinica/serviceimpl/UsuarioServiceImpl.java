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
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<UsuarioRS> listarUsuarios() {
        // Orden alfabetico A-Z: Collator con Locale "es" maneja tildes y mayus/minus
        // igual que localeCompare(a, b, 'es', { sensitivity: 'base' }) en JS.
        Collator collator = Collator.getInstance(new Locale("es"));
        collator.setStrength(Collator.SECONDARY);

        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getNombre, collator))
                .map(this::aPublico)
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioRS crearUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        this.validarUsuario(usuarioRq, true);

        String email = usuarioRq.getEmail().trim().toLowerCase();
        if (this.usuarioRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(usuarioRq.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(this.cifrarMD5(usuarioRq.getPassword()));
        usuario.setRol(usuarioRq.getRol() == null || usuarioRq.getRol().trim().isEmpty()
                ? "usuario" : usuarioRq.getRol().trim());
        usuario.setFechaRegistro(LocalDateTime.now());

        this.usuarioRepository.save(usuario);

        return this.aPublico(usuario);
    }

    @Override
    public UsuarioRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        if (usuarioRq == null || usuarioRq.getUsuarioId() == null) {
            throw new BadRequestException("El ID del usuario es obligatorio para actualizar");
        }

        Optional<Usuario> optUsuario = this.usuarioRepository.findById(usuarioRq.getUsuarioId());
        if (optUsuario.isEmpty()) {
            throw new BadRequestException("Usuario no encontrado");
        }

        this.validarUsuario(usuarioRq, false);

        Usuario usuario = optUsuario.get();

        if (usuarioRq.getNombre() != null && !usuarioRq.getNombre().trim().isEmpty()) {
            usuario.setNombre(usuarioRq.getNombre().trim());
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

        // El password es opcional en la actualizacion: si no llega, se conserva el hash anterior.
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().trim().isEmpty()) {
            usuario.setPassword(this.cifrarMD5(usuarioRq.getPassword()));
        }

        usuario.setFechaModificacion(LocalDateTime.now());

        this.usuarioRepository.save(usuario);

        return this.aPublico(usuario);
    }

    private void validarUsuario(UsuarioRq usuarioRq, boolean esCreacion) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        if (esCreacion || usuarioRq.getNombre() != null) {
            if (usuarioRq.getNombre() == null || usuarioRq.getNombre().trim().isEmpty()) {
                throw new BadRequestException("El nombre es obligatorio");
            }
            if (usuarioRq.getNombre().trim().length() < 3) {
                throw new BadRequestException("El nombre debe tener al menos 3 caracteres");
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

        if (esCreacion || usuarioRq.getPassword() != null) {
            if (usuarioRq.getPassword() == null || usuarioRq.getPassword().trim().isEmpty()) {
                if (esCreacion) {
                    throw new BadRequestException("El password es obligatorio");
                }
            } else if (usuarioRq.getPassword().length() < 4) {
                throw new BadRequestException("El password debe tener al menos 4 caracteres");
            }
        }
    }

    /**
     * Requisito 3: cifrar el password en Hash MD5 usando el proveedor nativo de Java.
     */
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

    /**
     * Requisito 2: nunca exponer el password. Se arma la respuesta publica sin ese campo.
     */
    private UsuarioRS aPublico(Usuario usuario) {
        UsuarioRS rs = new UsuarioRS();
        rs.setUsuarioId(usuario.getUsuarioId());
        rs.setNombre(usuario.getNombre());
        rs.setEmail(usuario.getEmail());
        rs.setRol(usuario.getRol());
        rs.setFechaRegistro(usuario.getFechaRegistro());
        rs.setFechaModificacion(usuario.getFechaModificacion());
        return rs;
    }
}
