package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/mascota")
public interface MascotaApi {
    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/listar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Mascota>> getMascotas()
            throws BadRequestException;

    /**
     * Metodo que busca una mascota por nombre.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Mascota> buscarMascotasPorNombre(
            @RequestParam String nombre)
            throws BadRequestException;


    @GetMapping(value = "/listar-ordenado",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotasOrdenadas(
            @RequestParam boolean ascendente
    )
            throws BadRequestException;


    /**
     * Lista las mascotas de un cliente.
     *
     * @param clienteId identificador del cliente.
     * @return 200 con las mascotas, o 404 si el cliente no existe.
     */
    @GetMapping(value = "/cliente/{clienteId:\\d+}", produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotasPorCliente(@PathVariable("clienteId") Long clienteId);

    /**
     * Consulta una mascota por su identificador.
     *
     * @param mascotaId identificador de la mascota.
     * @return 200 con la mascota, o 404 si no existe.
     */
    @GetMapping(value = "/{mascotaId:\\d+}", produces = {"application/json"})
    ResponseEntity<Mascota> consultarMascota(@PathVariable("mascotaId") Long mascotaId);

    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMascota(
            @RequestBody MascotaRq mascotaRq
    )
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMascota(
            @RequestBody MascotaRq mascotaRq
    )
            throws BadRequestException;
}
