package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio del catálogo de razas.
 */
@Service
public class RazaServiceImpl implements RazaService {

    @Autowired
    private RazaRepository razaRepository;

    @Override
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
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza actualizada correctamente");
        return respuesta;
    }

    private void validarObjetoEntrada(RazaRq razaRq) throws BadRequestException {
        if (razaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (razaRq.getNombre() == null || razaRq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la raza es obligatorio");
        }
        if (razaRq.getEspecie() == null || razaRq.getEspecie().isBlank()) {
            throw new BadRequestException("La especie de la raza es obligatoria");
        }
    }
}
