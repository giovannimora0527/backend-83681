package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
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

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAllByOrderByUserNameAsc();
    }

    @Override
    public MiRespuestaRS guardarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada
        this.validarObjetoEntrada(usuarioRq, true);

        // Paso 2. Validar que no exista ya un usuario con el mismo nombre de usuario
        if (this.usuarioRepository.findByUserName(usuarioRq.getUserName()).isPresent()) {
            throw new BadRequestException("Ya existe un usuario con el nombre de usuario: " + usuarioRq.getUserName());
        }

        // Paso 3. Crear la entidad a guardar
        Usuario usuario = new Usuario();
        usuario.setUserName(usuarioRq.getUserName());
        usuario.setPasswordHash(this.hashPassword(usuarioRq.getPassword()));
        usuario.setRol(usuarioRq.getRol());
        usuario.setEmail(usuarioRq.getEmail());
        usuario.setActivo(usuarioRq.getActivo() != null ? usuarioRq.getActivo() : true);
        usuario.setFechaCreacion(LocalDateTime.now());

        this.usuarioRepository.save(usuario);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Usuario guardado correctamente");

        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        // Paso 1. Validar el objeto de entrada (el password es opcional al actualizar)
        this.validarObjetoEntrada(usuarioRq, false);

        // Paso 2. Validar que el usuario a actualizar exista
        Optional<Usuario> optUsuario = this.usuarioRepository.findById(usuarioRq.getId());
        if (optUsuario.isEmpty()) {
            throw new BadRequestException("El usuario con ID " + usuarioRq.getId() + " no existe en la base de datos");
        }

        // Paso 3. Actualizar los datos
        Usuario usuario = optUsuario.get();
        usuario.setUserName(usuarioRq.getUserName());
        usuario.setRol(usuarioRq.getRol());
        usuario.setEmail(usuarioRq.getEmail());
        if (usuarioRq.getActivo() != null) {
            usuario.setActivo(usuarioRq.getActivo());
        }

        // Solo re-cifra la contraseña si mandaron una nueva
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()) {
            usuario.setPasswordHash(this.hashPassword(usuarioRq.getPassword()));
        }

        this.usuarioRepository.save(usuario);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Usuario actualizado correctamente");

        return respuesta;
    }

    /**
     * Calcula el hash SHA-256 de una contraseña en texto plano.
     * Usa únicamente clases del propio JDK (java.security), sin dependencias externas.
     */
    private String hashPassword(String passwordPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(passwordPlano.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 no disponible", e);
        }
    }

    private void validarObjetoEntrada(UsuarioRq usuarioRq, boolean esCreacion) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        if (usuarioRq.getUserName() == null || usuarioRq.getUserName().trim().isEmpty()) {
            throw new BadRequestException("El nombre de usuario no puede estar vacío");
        }

        if (esCreacion && (usuarioRq.getPassword() == null || usuarioRq.getPassword().trim().isEmpty())) {
            throw new BadRequestException("La contraseña no puede estar vacía");
        }

        if (!esCreacion && usuarioRq.getId() == null) {
            throw new BadRequestException("El ID del usuario es obligatorio para actualizar");
        }

        if (usuarioRq.getEmail() == null || usuarioRq.getEmail().trim().isEmpty()) {
            throw new BadRequestException("El email no puede estar vacío");
        }
    }
}