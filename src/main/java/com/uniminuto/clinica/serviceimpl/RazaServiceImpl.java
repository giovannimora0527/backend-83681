package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

@Service
public class RazaServiceImpl implements RazaService {

    @Autowired
    private RazaRepository razaRepository;

    @Override
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

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza actualizada correctamente");
        return respuesta;
    }

    @Override
    public List<Raza> listarRazas() throws BadRequestException {
        return razaRepository.findAll().stream().sorted((r1, r2) -> r1.getNombre().compareToIgnoreCase(r2
                .getNombre())).toList();
    }

    @Override
    public List<Raza> buscarRazas(String termino) {
        String busqueda = termino == null ? "" : termino.trim().toLowerCase(Locale.ROOT);
        return listarRazas().stream()
                .filter(raza -> busqueda.isEmpty()
                        || contiene(raza.getNombre(), busqueda)
                        || contiene(raza.getEspecie(), busqueda))
                .toList();
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
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
        }
    }
}
