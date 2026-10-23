package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.MedicoRq;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/medico")
public interface MedicoApi {

    @PostMapping(value = "/guardar", produces = "application/json", consumes = "application/json")
    ResponseEntity<MiRespuestaRS> guardarMedico(@RequestBody MedicoRq medicoRq);

    @PostMapping(value = "/actualizar", produces = "application/json", consumes = "application/json")
    ResponseEntity<MiRespuestaRS> actualizarMedico(@RequestBody MedicoRq medicoRq);

    @DeleteMapping(value = "/eliminar/{id}", produces = "application/json")
    ResponseEntity<MiRespuestaRS> eliminarMedico(@PathVariable("id") Long id);

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/all",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<List<Medico>> getMedicos()
            throws BadRequestException;

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/buscar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Medico> buscarMedico(
            @RequestParam String tipoDocumento,
            @RequestParam String numeroDocumento)
            throws BadRequestException;

    /**
     * Metodo test del servicio.
     *
     * @return Servicio funcionando correctamente.
     * @throws BadRequestException excepcion.
     */
    @GetMapping(value = "/buscarPorRegistroProfesional",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<Medico> buscarMedicoPorRegistroProfesional(
            @RequestParam String registroProfesional)
            throws BadRequestException;
}
