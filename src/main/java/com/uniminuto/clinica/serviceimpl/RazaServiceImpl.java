package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
=======
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de la lógica de negocio del catálogo de razas.
 */
@Service
public class RazaServiceImpl implements RazaService {

    @Autowired
    private RazaRepository razaRepository;

    @Override
<<<<<<< HEAD
    public MiRespuestaRS crearRazaNueva(RazaRq razaRq) throws BadRequestException {

        this.validarRazaRq(razaRq);
        Optional<Raza> optRaza = this.razaRepository.findByEspecieAndNombre(razaRq.getEspecie(), razaRq.getNombre());

        if (optRaza.isPresent()) {
            throw new BadRequestException("La raza ya existe para la especie especificada.");
        }

        Raza nuevaRaza = new Raza();
        nuevaRaza.setEspecie(razaRq.getEspecie());
        nuevaRaza.setNombre(razaRq.getNombre());
        nuevaRaza.setFechaCreacion(LocalDateTime.now());
        this.razaRepository.save(nuevaRaza);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza creada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarRaza(RazaRq razaRq) throws BadRequestException {
        if (razaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede ser nulo.");
        }
        if (razaRq.getRazaId() == null) {
            throw new BadRequestException("El ID de la raza es obligatorio para actualizar.");
        }
        validarRazaRq(razaRq);

        Raza existente = razaRepository.findById(razaRq.getRazaId())
                .orElseThrow(() -> new BadRequestException(
                        "La raza con ID " + razaRq.getRazaId() + " no existe."));

        Optional<Raza> duplicada = razaRepository.findByEspecieAndNombre(
                razaRq.getEspecie(), razaRq.getNombre());
        if (duplicada.isPresent() && !duplicada.get().getRazaId().equals(existente.getRazaId())) {
            throw new BadRequestException("La raza ya existe para la especie especificada.");
        }

        existente.setEspecie(razaRq.getEspecie());
        existente.setNombre(razaRq.getNombre());
        existente.setFechaModificacion(LocalDateTime.now());
        razaRepository.save(existente);

=======
    public List<Raza> listarRazas() {
        return razaRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public Raza buscarRazaPorNombre(String nombre) throws BadRequestException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre de la raza no puede estar vacío");
        }
        return razaRepository.findByNombre(nombre.trim())
                .orElseThrow(() -> new BadRequestException("No se encontró la raza con el nombre: " + nombre, HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarRaza(RazaRq razaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(razaRq);

        // Paso 2. Evito nombres duplicados
        if (razaRepository.findByNombre(razaRq.getNombre().trim()).isPresent()) {
            throw new BadRequestException("Ya existe una raza con el nombre " + razaRq.getNombre(), HttpStatus.CONFLICT);
        }

        // Paso 3. Creo la entidad raza a guardar
        Raza raza = new Raza();
        raza.setNombre(razaRq.getNombre().trim());
        raza.setEspecie(razaRq.getEspecie().trim());
        this.razaRepository.save(raza);

        // Paso 4. Retorno la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarRaza(RazaRq razaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(razaRq);
        if (razaRq.getRazaId() == null || razaRq.getRazaId() <= 0) {
            throw new BadRequestException("El ID de la raza no puede estar vacío ni ser menor o igual a cero");
        }

        // Paso 2. Verifico que la raza exista y que el nombre no pertenezca a otra
        Raza raza = this.razaRepository.findById(razaRq.getRazaId())
                .orElseThrow(() -> new BadRequestException("La raza con ID " + razaRq.getRazaId() + " no existe", HttpStatus.NOT_FOUND));
        if (razaRepository.findByNombre(razaRq.getNombre().trim())
                .filter(otra -> !otra.getRazaId().equals(raza.getRazaId())).isPresent()) {
            throw new BadRequestException("Ya existe otra raza con el nombre " + razaRq.getNombre(), HttpStatus.CONFLICT);
        }

        // Paso 3. Actualizo los datos
        raza.setNombre(razaRq.getNombre().trim());
        raza.setEspecie(razaRq.getEspecie().trim());
        this.razaRepository.save(raza);

        // Paso 4. Retorno la respuesta
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza actualizada correctamente");
        return respuesta;
    }

<<<<<<< HEAD
    @Override
    public List<Raza> listarRazas() throws BadRequestException {
        return razaRepository.findAll().stream().sorted((r1, r2) -> r1.getNombre().compareToIgnoreCase(r2
                .getNombre())).toList();
    }

    private void validarRazaRq(RazaRq razaRq) throws BadRequestException {
        if (razaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede ser nulo.");
        }
        if (razaRq.getNombre() == null || razaRq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la raza es obligatorio.");
        }
        if (razaRq.getEspecie() == null || razaRq.getEspecie().isBlank()) {
            throw new BadRequestException("La especie de la raza es obligatoria.");
=======
    private void validarObjetoEntrada(RazaRq razaRq) throws BadRequestException {
        if (razaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (razaRq.getNombre() == null || razaRq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la raza es obligatorio");
        }
        if (razaRq.getEspecie() == null || razaRq.getEspecie().isBlank()) {
            throw new BadRequestException("La especie de la raza es obligatoria");
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
        }
    }
}
