package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRs;
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


    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRs> guardarMascota(
            @RequestBody MascotaRq mascotaRq
    )
            throws BadRequestException;

    @PostMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRs> actualizarMascota(
            @RequestBody MascotaRq mascotaRq
    )
            throws BadRequestException;
}
