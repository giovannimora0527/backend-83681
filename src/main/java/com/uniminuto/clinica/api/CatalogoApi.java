package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Contrato REST de catálogos para los combos: clientes, mascotas del cliente y veterinarios.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/catalogo")
public interface CatalogoApi {

    /**
     * @return todos los clientes.
     */
    @GetMapping(value = "/clientes", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Cliente>> listarClientes();

    /**
     * @param id id del cliente.
     * @return detalle del cliente.
     */
    @GetMapping(value = "/clientes/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Cliente> consultarCliente(@PathVariable("id") Long id);

    /**
     * @param id id del cliente.
     * @return mascotas de ese cliente (combo dependiente).
     */
    @GetMapping(value = "/clientes/{id}/mascotas", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Mascota>> listarMascotasDeCliente(@PathVariable("id") Long id);

    /**
     * @param id id de la mascota.
     * @return detalle de la mascota.
     */
    @GetMapping(value = "/mascotas/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Mascota> consultarMascota(@PathVariable("id") Integer id);

    /**
     * @return todos los veterinarios.
     */
    @GetMapping(value = "/veterinarios", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Medico>> listarVeterinarios();
}
