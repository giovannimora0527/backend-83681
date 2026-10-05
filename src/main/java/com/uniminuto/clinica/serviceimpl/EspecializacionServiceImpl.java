package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    private final EspecializacionRepository repository;

    public EspecializacionServiceImpl(EspecializacionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Especializacion> listarEspecializaciones() {
        return repository.findAll();
    }

    @Override
    public Especializacion guardarEspecializacion(Especializacion especializacion) {
        validar(especializacion);
        if (repository.findByCodigoEspecializacion(especializacion.getCodigoEspecializacion()).isPresent()) {
            throw new BadRequestException("Ya existe una especialización con ese código");
        }
        especializacion.setId(null);
        return repository.save(especializacion);
    }

    @Override
    public Especializacion actualizarEspecializacion(Especializacion especializacion) {
        validar(especializacion);
        if (especializacion.getId() == null || !repository.existsById(especializacion.getId())) {
            throw new BadRequestException("La especialización que desea actualizar no existe");
        }
        if (repository.findByCodigoEspecializacion(especializacion.getCodigoEspecializacion())
                .filter(existing -> !existing.getId().equals(especializacion.getId())).isPresent()) {
            throw new BadRequestException("Ya existe una especialización con ese código");
        }
        return repository.save(especializacion);
    }

    private void validar(Especializacion especializacion) {
        if (especializacion == null || especializacion.getNombre() == null
                || especializacion.getNombre().isBlank() || especializacion.getCodigoEspecializacion() == null
                || especializacion.getCodigoEspecializacion().isBlank()) {
            throw new BadRequestException("Nombre y código de especialización son obligatorios");
        }
    }
}
