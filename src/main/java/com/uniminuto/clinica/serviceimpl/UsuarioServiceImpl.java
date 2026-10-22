package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio de usuarios.
 * La contraseña se guarda como hash MD5 y el username y el email son únicos.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> listarUsuarios() throws BadRequestException {
        return this.usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
    }

    @Override
    public MiRespuestaRS guardarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(usuarioRq);

        // Paso 2. Evito username y email duplicados
        if (this.usuarioRepository.findByUsername(usuarioRq.getUsername()).isPresent()) {
            throw new BadRequestException("El username ya existe.", HttpStatus.CONFLICT);
        }
        if (this.usuarioRepository.findByEmail(usuarioRq.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado.", HttpStatus.CONFLICT);
        }

        // Paso 3. Creo el usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(usuarioRq.getUsername());
        nuevoUsuario.setPasswordHash(PasswordUtil.generateMd5(usuarioRq.getPassword()));
        nuevoUsuario.setRol(usuarioRq.getRol());
        nuevoUsuario.setEmail(usuarioRq.getEmail());
        nuevoUsuario.setActivo(usuarioRq.getActivo() != null ? usuarioRq.getActivo() : Boolean.TRUE);
        this.usuarioRepository.save(nuevoUsuario);

        // Paso 4. Retorno la respuesta
        return respuesta(HttpStatus.CREATED.value(), "Usuario creado con éxito.");
    }

    @Override
    public MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        // Paso 1. Valido el ID de entrada
        if (usuarioRq == null || usuarioRq.getId() == null || usuarioRq.getId() <= 0) {
            throw new BadRequestException("El ID del usuario es obligatorio para actualizar.");
        }

        // Paso 2. Verifico que el usuario exista
        Usuario usuario = this.usuarioRepository.findById(usuarioRq.getId())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado con el ID: " + usuarioRq.getId(), HttpStatus.NOT_FOUND));

        // Paso 3. Actualizo solo los campos enviados
        if (usuarioRq.getUsername() != null && !usuarioRq.getUsername().isBlank()) {
            if (this.usuarioRepository.findByUsername(usuarioRq.getUsername())
                    .filter(otro -> !otro.getId().equals(usuario.getId())).isPresent()) {
                throw new BadRequestException("El username ya existe.", HttpStatus.CONFLICT);
            }
            usuario.setUsername(usuarioRq.getUsername());
        }
        if (usuarioRq.getEmail() != null && !usuarioRq.getEmail().isBlank()) {
            if (this.usuarioRepository.findByEmail(usuarioRq.getEmail())
                    .filter(otro -> !otro.getId().equals(usuario.getId())).isPresent()) {
                throw new BadRequestException("El email ya está registrado.", HttpStatus.CONFLICT);
            }
            usuario.setEmail(usuarioRq.getEmail());
        }
        if (usuarioRq.getRol() != null && !usuarioRq.getRol().isBlank()) {
            usuario.setRol(usuarioRq.getRol());
        }
        if (usuarioRq.getActivo() != null) {
            usuario.setActivo(usuarioRq.getActivo());
        }
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()) {
            usuario.setPasswordHash(PasswordUtil.generateMd5(usuarioRq.getPassword()));
        }
        this.usuarioRepository.save(usuario);

        // Paso 4. Retorno la respuesta
        return respuesta(HttpStatus.OK.value(), "Usuario actualizado con éxito.");
    }

    private MiRespuestaRS respuesta(int status, String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(status);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(UsuarioRq usuarioRq) throws BadRequestException {
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
    }
}
