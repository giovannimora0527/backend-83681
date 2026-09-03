package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MascotaServiceImpl implements MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public List<Mascota> getListarMascotas() {
        return mascotaRepository.findAll();
    }

    @Override
    public Mascota buscarMascotaPorNombre(String nombre) throws BadRequestException {
       if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre de la mascota no puede estar vacío");
        }
        Mascota mascota = mascotaRepository.findByNombreMascota(nombre);
        if (mascota == null) {
            throw new BadRequestException("No se encontró la mascota con el nombre: " + nombre);
        }
        return mascota;
    }
}
