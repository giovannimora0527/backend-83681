package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.RazaRq;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RazaServiceImpl implements RazaService {

    private final RazaRepository razaRepository;
    private final MascotaRepository mascotaRepository;

    public RazaServiceImpl(RazaRepository razaRepository, MascotaRepository mascotaRepository) {
        this.razaRepository = razaRepository;
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public List<Raza> listarRazas() {
        return razaRepository.findAll();
    }

    @Override
    @Transactional
    public MiRespuestaRS crearRaza(RazaRq rq) {
        validar(rq);
        if (razaRepository.existsByNombreIgnoreCaseAndEspecieIgnoreCase(rq.getNombre(), rq.getEspecie())) {
            throw new ConflictException("Ya existe esa raza para la especie indicada.");
        }

        Raza raza = new Raza();
        aplicarCambios(raza, rq);
        raza.setFechaCreacion(LocalDateTime.now());
        raza = razaRepository.save(raza);
        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Raza creada correctamente.", raza.getRazaId().longValue());
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarRaza(RazaRq rq) {
        validar(rq);
        if (rq.getRazaId() == null) {
            throw new BadRequestException("El identificador de la raza es obligatorio.");
        }
        Raza raza = razaRepository.findById(rq.getRazaId())
                .orElseThrow(() -> new NotFoundException("La raza seleccionada no existe."));
        if (razaRepository.existsByNombreIgnoreCaseAndEspecieIgnoreCaseAndRazaIdNot(
                rq.getNombre(), rq.getEspecie(), rq.getRazaId())) {
            throw new ConflictException("Ya existe otra raza para esa especie.");
        }

        aplicarCambios(raza, rq);
        raza.setFechaModificacion(LocalDateTime.now());
        razaRepository.save(raza);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Raza actualizada correctamente.",
                raza.getRazaId().longValue());
    }

    @Override
    @Transactional
    public MiRespuestaRS eliminarRaza(Integer id) {
        Raza raza = razaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La raza seleccionada no existe."));
        if (mascotaRepository.existsByRaza_RazaId(id)) {
            throw new ConflictException("No se puede eliminar la raza porque tiene mascotas asociadas.");
        }
        razaRepository.delete(raza);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Raza eliminada correctamente.", id.longValue());
    }

    private void validar(RazaRq rq) {
        if (rq == null || vacio(rq.getNombre()) || vacio(rq.getEspecie())) {
            throw new BadRequestException("El nombre y la especie de la raza son obligatorios.");
        }
    }

    private void aplicarCambios(Raza raza, RazaRq rq) {
        raza.setNombre(rq.getNombre().trim());
        raza.setEspecie(rq.getEspecie().trim());
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
