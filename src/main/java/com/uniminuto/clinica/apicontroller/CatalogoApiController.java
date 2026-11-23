package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CatalogoApi;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.service.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de catálogos. Delega en {@link CatalogoService}.
 */
@RestController
public class CatalogoApiController implements CatalogoApi {

    /** Servicio de catálogos. */
    @Autowired
    private CatalogoService catalogoService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(catalogoService.listarClientes());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Cliente> consultarCliente(Long id) {
        return ResponseEntity.ok(catalogoService.consultarCliente(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Mascota>> listarMascotasDeCliente(Long id) {
        return ResponseEntity.ok(catalogoService.listarMascotasDeCliente(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Mascota> consultarMascota(Integer id) {
        return ResponseEntity.ok(catalogoService.consultarMascota(id));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medico>> listarVeterinarios() {
        return ResponseEntity.ok(catalogoService.listarVeterinarios());
    }
}
