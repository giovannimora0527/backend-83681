package com.uniminuto.clinica.modulos.gestion;

import com.uniminuto.clinica.support.In;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "0. Clientes y usuarios", description = "Clientes: lectura ADMINISTRADOR/RECEPCION/MEDICO, escritura ADMINISTRADOR/RECEPCION. "
        + "Usuarios: solo ADMINISTRADOR.")
public class GestionController {

    private final GestionService service;

    public GestionController(GestionService service) {
        this.service = service;
    }

    @Operation(summary = "Lista clientes con búsqueda por documento, nombre o teléfono")
    @GetMapping("/cliente/gestion")
    public List<Map<String, Object>> clientes(@RequestParam(required = false) String q,
                                              @RequestParam(required = false) Boolean activo) {
        return service.clientes(q, activo);
    }

    @Operation(summary = "Registra un cliente")
    @PostMapping("/cliente/gestion")
    public Map<String, Object> crearCliente(@RequestBody Map<String, Object> body) {
        return service.crearCliente(body);
    }

    @Operation(summary = "Edita un cliente")
    @PutMapping("/cliente/gestion/{id}")
    public Map<String, Object> editarCliente(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.editarCliente(id, body);
    }

    @Operation(summary = "Activa o inactiva un cliente")
    @PatchMapping("/cliente/gestion/{id}/estado")
    public Map<String, Object> estadoCliente(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.estadoCliente(id, In.bool(body, "activo", true));
    }

    @Operation(summary = "Lista perfiles disponibles")
    @GetMapping("/usuario/gestion/perfiles")
    public List<Map<String, Object>> perfiles() {
        return service.perfiles();
    }

    @Operation(summary = "Lista usuarios del sistema con su perfil")
    @GetMapping("/usuario/gestion")
    public List<Map<String, Object>> usuarios(@RequestParam(required = false) String q) {
        return service.usuarios(q);
    }

    @Operation(summary = "Crea un usuario (username, email, perfilId, password)")
    @PostMapping("/usuario/gestion")
    public Map<String, Object> crearUsuario(@RequestBody Map<String, Object> body) {
        return service.crearUsuario(body);
    }

    @Operation(summary = "Edita un usuario; la contraseña es opcional")
    @PutMapping("/usuario/gestion/{id}")
    public Map<String, Object> editarUsuario(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.editarUsuario(id, body);
    }

    @Operation(summary = "Activa o inactiva un usuario (cierra sus sesiones al inactivarlo)")
    @PatchMapping("/usuario/gestion/{id}/estado")
    public Map<String, Object> estadoUsuario(@PathVariable long id, @RequestBody Map<String, Object> body) {
        return service.estadoUsuario(id, In.bool(body, "activo", true));
    }
}
