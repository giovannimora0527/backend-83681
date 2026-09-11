package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> listarUsuarios() throws BadRequestException {
        try {
            return this.usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
        } catch (Exception e) {
            throw new BadRequestException("Error al listar los usuarios: " + e.getMessage());
        }
    }

    @Override
    public UsuarioRS guardarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("La solicitud del usuario es obligatoria.");
        }

        if (usuarioRq.getUsername() == null || usuarioRq.getUsername().isBlank()) {
            throw new BadRequestException("El username es obligatorio.");
        }

        if (usuarioRq.getPassword() == null || usuarioRq.getPassword().isBlank()) {
            throw new BadRequestException("La contraseña es obligatoria.");
        }

        if (usuarioRq.getRol() == null || usuarioRq.getRol().isBlank()) {
            throw new BadRequestException("El rol es obligatorio.");
        }

        if (usuarioRq.getEmail() == null || usuarioRq.getEmail().isBlank()) {
            throw new BadRequestException("El email es obligatorio.");
        }

        if (this.usuarioRepository.findByUsername(usuarioRq.getUsername()).isPresent()) {
            throw new BadRequestException("El username ya existe.");
        }

        if (this.usuarioRepository.findByEmail(usuarioRq.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado.");
        }

        try {
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(usuarioRq.getUsername());
            nuevoUsuario.setPasswordHash(PasswordUtil.generateMd5(usuarioRq.getPassword()));
            nuevoUsuario.setRol(usuarioRq.getRol());
            nuevoUsuario.setEmail(usuarioRq.getEmail());
            nuevoUsuario.setActivo(usuarioRq.getActivo() != null ? usuarioRq.getActivo() : true);

            this.usuarioRepository.save(nuevoUsuario);

            UsuarioRS respuesta = new UsuarioRS();
            respuesta.setStatus(HttpStatus.CREATED.value());
            respuesta.setMessage("Usuario creado con éxito.");
            return respuesta;

        } catch (Exception e) {
            throw new BadRequestException("Error al guardar el usuario: " + e.getMessage());
        }
    }

    @Override
    public UsuarioRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("La solicitud del usuario es obligatoria.");
        }

        if (usuarioRq.getId() == null) {
            throw new BadRequestException("El ID del usuario es obligatorio para actualizar.");
        }

        Usuario usuarioExistente = this.usuarioRepository.findById(usuarioRq.getId())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado con el ID: " + usuarioRq.getId()));

        if (usuarioRq.getUsername() != null && !usuarioRq.getUsername().isBlank()) {
            if (!usuarioRq.getUsername().equals(usuarioExistente.getUsername())
                    && this.usuarioRepository.findByUsername(usuarioRq.getUsername()).isPresent()) {
                throw new BadRequestException("El username ya existe.");
            }
            usuarioExistente.setUsername(usuarioRq.getUsername());
        }

        if (usuarioRq.getRol() != null && !usuarioRq.getRol().isBlank()) {
            usuarioExistente.setRol(usuarioRq.getRol());
        }

        if (usuarioRq.getEmail() != null && !usuarioRq.getEmail().isBlank()) {
            if (!usuarioRq.getEmail().equalsIgnoreCase(usuarioExistente.getEmail())
                    && this.usuarioRepository.findByEmail(usuarioRq.getEmail()).isPresent()) {
                throw new BadRequestException("El email ya está registrado.");
            }
            usuarioExistente.setEmail(usuarioRq.getEmail());
        }

        if (usuarioRq.getActivo() != null) {
            usuarioExistente.setActivo(usuarioRq.getActivo());
        }

        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()) {
            usuarioExistente.setPasswordHash(PasswordUtil.generateMd5(usuarioRq.getPassword()));
        }

        try {
            this.usuarioRepository.save(usuarioExistente);

            UsuarioRS respuesta = new UsuarioRS();
            respuesta.setStatus(HttpStatus.OK.value());
            respuesta.setMessage("Usuario actualizado con éxito.");
            return respuesta;

        } catch (Exception e) {
            throw new BadRequestException("Error al actualizar el usuario: " + e.getMessage());
        }
    }
}
