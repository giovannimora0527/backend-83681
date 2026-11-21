package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.repository.PerfilRepository;
import java.time.LocalDateTime;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioApiController implements UsuarioApi {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilRepository perfilRepository;

    @Override
    public ResponseEntity<List<Perfil>> listarPerfiles() {
        return ResponseEntity.ok(perfilRepository.findAll());
    }

    @Override
    public ResponseEntity<Perfil> habilitarPerfilDespacho() {
        Perfil perfil = perfilRepository.findAll().stream()
            .filter(p -> "DESPACHO_MEDICO".equalsIgnoreCase(p.getNombre()))
            .findFirst().orElseGet(() -> {
                Perfil nuevo = new Perfil();
                nuevo.setNombre("DESPACHO_MEDICO");
                nuevo.setFechaCreacion(LocalDateTime.now());
                return nuevo;
            });
        perfil.setActivo(true);
        perfil.setFechaModificacion(LocalDateTime.now());
        return ResponseEntity.ok(perfilRepository.save(perfil));
    }


    @Override
    public ResponseEntity<List<UsuarioRs>> getUsuarios() throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.obtenerUsuariosOrdenados());
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardar(UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.crearUsuario(usuarioRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizar(UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.actualizarUsuario(usuarioRq));
    }
}
