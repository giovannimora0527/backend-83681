package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PerfilRs;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.repository.PerfilRepository;
import com.uniminuto.clinica.repository.SessionRepository;
=======
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de la lógica de negocio de usuarios.
 * La contraseña se guarda como hash MD5 y el username y el email son únicos.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;

<<<<<<< HEAD
    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder,
            SessionRepository sessionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public List<UsuarioRs> obtenerUsuariosOrdenados() {
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByUsernameAsc();
        return convertirUsuarioToUsuarioRs(usuarios);
    }

    @Override
    public List<PerfilRs> obtenerPerfilesActivos() {
        return perfilRepository.findAllByActivoTrueOrderByNombreAsc().stream().map(perfil -> {
            PerfilRs respuesta = new PerfilRs();
            respuesta.setId(perfil.getPerfilId());
            respuesta.setNombre(perfil.getNombre());
            return respuesta;
        }).toList();
    }

    private List<UsuarioRs> convertirUsuarioToUsuarioRs(List<Usuario> usuarios) {
        return usuarios.stream().map(usuario -> {
            UsuarioRs usuarioRs = new UsuarioRs();
            usuarioRs.setId(usuario.getId());
            usuarioRs.setUsername(usuario.getUsername());
            usuarioRs.setRol(usuario.getPerfil().getNombre());
            usuarioRs.setFechaCreacion(usuario.getFechaCreacion());
            usuarioRs.setActivo(usuario.getActivo());
            usuarioRs.setEmail(usuario.getEmail());
            return usuarioRs;
        }).toList();
    }

    @Override
    @Transactional
    public MiRespuestaRS crearUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq, true);

        String username = usuarioRq.getUsername().trim();
        String email = usuarioRq.getEmail().trim();
        if (this.usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }
        if (this.usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("El correo electrónico ya existe");
        }

        Optional<Perfil> optPerfil = this.perfilRepository
                .findById(usuarioRq.getPerfilId());
        if (optPerfil.isEmpty()) {
            throw new BadRequestException("El perfil no existe");
        }

        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setUsername(username);
        usuarioNuevo.setPasswordHash(passwordEncoder.encode(usuarioRq.getPassword()));
        usuarioNuevo.setPerfil(optPerfil.get());
        usuarioNuevo.setEmail(email);
        usuarioNuevo.setActivo(true);
        usuarioNuevo.setFechaCreacion(LocalDateTime.now());

        usuarioRepository.save(usuarioNuevo);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Usuario creado exitosamente");
        rta.setStatus(200);

        return rta;
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq, false);

        Optional<Usuario> optUser = this.usuarioRepository.findById(usuarioRq.getId());
        if (optUser.isEmpty()) {
            throw new BadRequestException("El usuario no existe");
        }
        if (usuarioRq.getActivo() == null) {
            throw new BadRequestException("El estado del usuario es obligatorio");
        }

        Usuario usuarioExistente = optUser.get();
        Integer perfilAnteriorId = usuarioExistente.getPerfil().getPerfilId();
        boolean eraAdminActivo = Boolean.TRUE.equals(usuarioExistente.getActivo())
                && "ADMINISTRADOR".equalsIgnoreCase(usuarioExistente.getPerfil().getNombre());
        String username = usuarioRq.getUsername().trim();
        String email = usuarioRq.getEmail().trim();
        if (!usuarioExistente.getUsername().equalsIgnoreCase(username)) {
            if (this.usuarioRepository.existsByUsernameIgnoreCase(username)) {
                throw new BadRequestException("El nombre de usuario ya existe");
            }
        }
        usuarioExistente.setUsername(username);
        if (!usuarioExistente.getEmail().equalsIgnoreCase(email)) {
            if (this.usuarioRepository.existsByEmailIgnoreCase(email)) {
                throw new BadRequestException("El correo electrónico ya existe");
            }
        }
        Optional<Perfil> optPerfil = this.perfilRepository
                .findById(usuarioRq.getPerfilId());
        if (optPerfil.isEmpty()) {
            throw new BadRequestException("El perfil no existe");
        }

        boolean seguiraAdminActivo = Boolean.TRUE.equals(usuarioRq.getActivo())
                && "ADMINISTRADOR".equalsIgnoreCase(optPerfil.get().getNombre());
        if (eraAdminActivo && !seguiraAdminActivo
                && usuarioRepository.countByPerfil_PerfilIdAndActivoTrue(perfilAnteriorId) <= 1) {
            throw new BadRequestException("No puedes desactivar ni cambiar el rol del último administrador activo.");
        }

        boolean cambioDeSeguridad = !perfilAnteriorId.equals(optPerfil.get().getPerfilId())
                || !Boolean.TRUE.equals(usuarioRq.getActivo())
                || (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank());

        usuarioExistente.setEmail(email);
        usuarioExistente.setActivo(usuarioRq.getActivo());
        usuarioExistente.setPerfil(optPerfil.get());
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(usuarioRq.getPassword()));
        }
        usuarioRepository.save(usuarioExistente);
        if (cambioDeSeguridad) {
            sessionRepository.deleteByUserId(usuarioExistente.getId());
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
        }
        this.usuarioRepository.save(usuario);

<<<<<<< HEAD
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Usuario actualizado exitosamente");
        rta.setStatus(200);

        return rta;
    }

    private void validarUsuario(UsuarioRq usuarioRq, boolean passwordObligatoria) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("El usuario no puede estar vacío");
        }
        if (usuarioRq.getUsername() == null || usuarioRq.getUsername().isBlank()
                || usuarioRq.getUsername().trim().length() > 50) {
            throw new BadRequestException("El nombre de usuario no puede estar vacío");
        }
        if (passwordObligatoria && (usuarioRq.getPassword() == null || usuarioRq.getPassword().isBlank())) {
            throw new BadRequestException("La contraseña no puede estar vacía");
        }
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()
                && (usuarioRq.getPassword().length() < 6
                || usuarioRq.getPassword().getBytes(StandardCharsets.UTF_8).length > 72)) {
            throw new BadRequestException("La contraseña debe tener entre 6 y 72 caracteres");
        }
        if (usuarioRq.getRol() == null || usuarioRq.getRol().isBlank()) {
            throw new BadRequestException("El rol no puede estar vacío");
        }
        if (usuarioRq.getEmail() == null || usuarioRq.getEmail().isBlank()
                || !usuarioRq.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new BadRequestException("Ingresa un correo electrónico válido");
        }
        if (usuarioRq.getPerfilId() == null) {
            throw new BadRequestException("Selecciona un perfil para el usuario");
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
        }
    }
}
