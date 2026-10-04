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

@Service
public class RazaServiceImpl implements RazaService {

    @Autowired
    private RazaRepository razaRepository;

    @Override
    public List<Raza> listar() {
        return razaRepository.findAll();
    }

    @Override
    public MiRespuestaRS guardar(RazaRq rq) throws BadRequestException {
        this.validar(rq);

        Raza raza = new Raza();
        raza.setNombre(rq.getNombre());
        raza.setEspecie(rq.getEspecie());
        raza.setFechaCreacion(LocalDateTime.now());
        raza.setFechaModificacion(LocalDateTime.now());

        razaRepository.save(raza);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizar(RazaRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getRazaId() == null) {
            throw new BadRequestException("El ID de la raza es obligatorio para actualizar");
        }

        Optional<Raza> optRaza = razaRepository.findById(rq.getRazaId());
        if (optRaza.isEmpty()) {
            throw new BadRequestException("La raza con ID " + rq.getRazaId() + " no existe");
        }

        Raza raza = optRaza.get();
        raza.setNombre(rq.getNombre());
        raza.setEspecie(rq.getEspecie());
        raza.setFechaModificacion(LocalDateTime.now());

        razaRepository.save(raza);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Raza actualizada correctamente");
        return respuesta;
    }

    private void validar(RazaRq rq) throws BadRequestException {
        if (rq == null || rq.getNombre() == null || rq.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la raza es obligatorio");
        }
    }
}
