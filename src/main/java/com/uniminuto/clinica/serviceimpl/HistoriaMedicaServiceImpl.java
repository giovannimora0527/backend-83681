// serviceimpl/HistoriaMedicaServiceImpl.java
package com.uniminuto.clinica.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;

@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public List<HistoriaMedica> listar() {
        return historiaMedicaRepository.findAll();
    }

    @Override
    public MiRespuestaRS guardarHistoria(HistoriaMedicaRq rq) throws BadRequestException {
        this.validar(rq);
        Mascota mascota = this.obtenerMascota(rq.getMascotaId());

        HistoriaMedica historia = new HistoriaMedica();
        historia.setMascota(mascota);
        historia.setFechaCreacion(LocalDateTime.now());
        historia.setObservacionesGenerales(rq.getObservacionesGenerales());

        historiaMedicaRepository.save(historia);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia médica guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarHistoria(HistoriaMedicaRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio para actualizar");
        }

        Optional<HistoriaMedica> optHistoria = historiaMedicaRepository.findById(rq.getId());
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia médica con ID " + rq.getId() + " no existe");
        }

        Mascota mascota = this.obtenerMascota(rq.getMascotaId());

        HistoriaMedica historia = optHistoria.get();
        historia.setMascota(mascota);
        historia.setObservacionesGenerales(rq.getObservacionesGenerales());

        historiaMedicaRepository.save(historia);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia médica actualizada correctamente");
        return respuesta;
    }

    private void validar(HistoriaMedicaRq rq) throws BadRequestException {
        if (rq == null || rq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota es obligatorio");
        }
    }

    private Mascota obtenerMascota(Long id) throws BadRequestException {
        Optional<Mascota> optMascota = mascotaRepository.findById(id);
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + id + " no existe");
        }
        return optMascota.get();
    }
}