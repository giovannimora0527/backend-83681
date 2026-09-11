package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las operaciones de usuarios.
 * Expone endpoints para crear, listar y actualizar usuarios.
 *
 * @author tu_nombre
 * @version 1.0
 */
@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UsuarioApiController implements UsuarioApi {

    /** Servicio inyectado para la lógica de negocio de usuarios. */
    @Autowired
    private UsuarioService usuarioService;

    /**
     * {@inheritDoc}
     * Endpoint POST para crear un nuevo usuario.
     * La contraseña se cifra automáticamente con MD5.
     *
     * URL en Postman: POST http://localhost:8080/clinica/v1/usuario/crear
     */
    @Override
    @PostMapping("/crear")
    public ResponseEntity<MiRespuestaRS> crear(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Usuario creado exitosamente");
        respuesta.setData(nuevoUsuario);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * {@inheritDoc}
     * Endpoint GET para listar todos los usuarios ordenados alfabéticamente.
     * NO muestra el password por seguridad.
     *
     * URL en Postman: GET http://localhost:8080/clinica/v1/usuario/listar
     */
    @Override
    @GetMapping("/listar")
    public ResponseEntity<MiRespuestaRS> listar() {
        List<Usuario> lista = usuarioService.listarUsuarios();

        // Eliminar passwords de la respuesta
        lista.forEach(usuario -> usuario.setPasswordHash(null));

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Usuarios obtenidos correctamente");
        respuesta.setData(lista);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * {@inheritDoc}
     * Endpoint POST para actualizar un usuario existente.
     *
     * URL en Postman: POST http://localhost:8080/clinica/v1/usuario/actualizar
     */
    @Override
    @PostMapping("/actualizar")
    public ResponseEntity<MiRespuestaRS> actualizar(@RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(usuario.getId(), usuario);

        usuarioActualizado.setPasswordHash(null);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Usuario actualizado exitosamente");
        respuesta.setData(usuarioActualizado);

        return ResponseEntity.ok(respuesta);
    }
}