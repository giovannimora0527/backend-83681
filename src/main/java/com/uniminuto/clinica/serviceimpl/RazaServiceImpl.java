package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.RazaRepository;
import com.uniminuto.clinica.service.RazaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RazaServiceImpl implements RazaService {

    private final RazaRepository repository;

    public RazaServiceImpl(RazaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Raza> listarRazas() {
        return repository.findAll();
    }

    @Override
    public Raza guardarRaza(Raza raza) {
        validar(raza);
        raza.setRazaId(null);
        return repository.save(raza);
    }

    @Override
    public Raza actualizarRaza(Raza raza) {
        validar(raza);
        if (raza.getRazaId() == null || !repository.existsById(raza.getRazaId())) {
            throw new BadRequestException("La raza que desea actualizar no existe");
        }
        return repository.save(raza);
    }

    private void validar(Raza raza) {
        if (raza == null || raza.getNombre() == null || raza.getNombre().isBlank()
                || raza.getEspecie() == null || raza.getEspecie().isBlank()) {
            throw new BadRequestException("Nombre y especie son obligatorios");
        }
    }
}
