package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ClienteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/cliente")
public interface ClienteApi {

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/all",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Cliente>> getClientes()
            throws BadRequestException;


    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/cliente-documento",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Cliente> getClientesByNumeroDocumento(
            @RequestParam String numeroDocumento
    )
            throws BadRequestException;

    /**
     * Consulta un cliente por su identificador.
     *
     * @param clienteId identificador del cliente.
     * @return cliente encontrado (200) o 404 si no existe.
     */
    @GetMapping(value = "/{clienteId}", produces = {"application/json"})
    ResponseEntity<Cliente> consultarCliente(@PathVariable("clienteId") Long clienteId);

    /**
     * Lista las mascotas de un cliente. Alimenta el combo dependiente
     * Cliente → Mascota de los formularios de citas, historias y fórmulas.
     *
     * @param clienteId identificador del cliente.
     * @return mascotas del cliente (200) o 404 si el cliente no existe.
     */
    @GetMapping(value = "/{clienteId}/mascotas", produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotasCliente(@PathVariable("clienteId") Long clienteId);

    @PostMapping(value = "/guardar", produces = "application/json", consumes = "application/json")
    ResponseEntity<MiRespuestaRS> guardarCliente(@RequestBody ClienteRq rq);

    @PostMapping(value = "/actualizar", produces = "application/json", consumes = "application/json")
    ResponseEntity<MiRespuestaRS> actualizarCliente(@RequestBody ClienteRq rq);

    @DeleteMapping(value = "/eliminar/{id}", produces = "application/json")
    ResponseEntity<MiRespuestaRS> eliminarCliente(@PathVariable("id") Long id);
}
