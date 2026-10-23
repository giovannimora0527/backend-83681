package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<UsuarioRs> obtenerUsuariosOrdenados() {
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByUsernameAsc();
        return convertirUsuarioToUsuarioRs(usuarios);
    }

    private List<UsuarioRs> convertirUsuarioToUsuarioRs(List<Usuario> usuarios) {
        return usuarios.stream().map(usuario -> {
            UsuarioRs usuarioRs = new UsuarioRs();
            usuarioRs.setId(usuario.getId());
            usuarioRs.setUsername(usuario.getUsername());
            usuarioRs.setRol(usuario.getRol());
            usuarioRs.setFechaCreacion(usuario.getFechaCreacion());
            usuarioRs.setActivo(usuario.getActivo());
            usuarioRs.setEmail(usuario.getEmail());
            return usuarioRs;
        }).toList();
    }

    @Override
    public MiRespuestaRS crearUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq, true);

        if (this.usuarioRepository.existsByUsername(usuarioRq.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }
        if (this.usuarioRepository.existsByEmail(usuarioRq.getEmail())) {
            throw new BadRequestException("El correo electrónico ya existe");
        }

        String claveMD5 = PasswordUtil.generarMD5(usuarioRq.getPassword());
        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setUsername(usuarioRq.getUsername());
        usuarioNuevo.setPasswordHash(claveMD5);
        usuarioNuevo.setRol(usuarioRq.getRol());
        usuarioNuevo.setEmail(usuarioRq.getEmail());
        usuarioNuevo.setActivo(usuarioRq.getActivo() == null ? Boolean.TRUE : usuarioRq.getActivo());
        usuarioNuevo.setFechaCreacion(LocalDateTime.now());

        usuarioNuevo = usuarioRepository.save(usuarioNuevo);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Usuario creado exitosamente");
        rta.setStatus(200);
        rta.setId(usuarioNuevo.getId());

        return rta;
    }

    @Override
    public MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq, false);

        if (usuarioRq.getId() == null) {
            throw new BadRequestException("El identificador del usuario es obligatorio");
        }
        Usuario usuarioExistente = usuarioRepository.findById(usuarioRq.getId())
                .orElseThrow(() -> new NotFoundException("El usuario no existe"));
        if (!usuarioExistente.getUsername().equals(usuarioRq.getUsername())) {
            if (this.usuarioRepository.existsByUsername(usuarioRq.getUsername())) {
                throw new BadRequestException("El nombre de usuario ya existe");
            }
        }
        usuarioExistente.setUsername(usuarioRq.getUsername());
        if (!usuarioExistente.getEmail().equals(usuarioRq.getEmail())) {
            if (this.usuarioRepository.existsByEmail(usuarioRq.getEmail())) {
                throw new BadRequestException("El correo electrónico ya existe");
            }
        }
        usuarioExistente.setEmail(usuarioRq.getEmail());
        usuarioExistente.setRol(usuarioRq.getRol());
        usuarioExistente.setActivo(usuarioRq.getActivo());
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()) {
            validarContrasena(usuarioRq.getPassword());
            usuarioExistente.setPasswordHash(PasswordUtil.generarMD5(usuarioRq.getPassword()));
        }
        usuarioRepository.save(usuarioExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Usuario actualizado exitosamente");
        rta.setStatus(200);
        rta.setId(usuarioExistente.getId());

        return rta;
    }

    @Override
    public MiRespuestaRS eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El usuario no existe"));
        usuarioRepository.delete(usuario);
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Usuario eliminado exitosamente");
        respuesta.setId(id);
        return respuesta;
    }

    private void validarUsuario(UsuarioRq usuarioRq, boolean requiereContrasena) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("El usuario no puede estar vacío");
        }
        if (usuarioRq.getUsername() == null || usuarioRq.getUsername().isEmpty()) {
            throw new BadRequestException("El nombre de usuario no puede estar vacío");
        }
        if (requiereContrasena && (usuarioRq.getPassword() == null || usuarioRq.getPassword().isEmpty())) {
            throw new BadRequestException("La contraseña no puede estar vacía");
        }
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()) {
            validarContrasena(usuarioRq.getPassword());
        }
        if (usuarioRq.getRol() == null || usuarioRq.getRol().isEmpty()) {
            throw new BadRequestException("El rol no puede estar vacío");
        }
        if (usuarioRq.getEmail() == null || usuarioRq.getEmail().isEmpty()) {
            throw new BadRequestException("El correo electrónico no puede estar vacío");
        }
    }

    private void validarContrasena(String password) {
        if (password.length() < 6) {
            throw new BadRequestException("La contraseña debe tener al menos 6 caracteres");
        }
    }
}
