// serviceimpl/AnotacionHistoriaServiceImpl.java
package com.uniminuto.clinica.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;

@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Override
    public List<AnotacionHistoria> listarPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("La fecha inicial y la fecha final son obligatorias");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return anotacionHistoriaRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }

    @Override
    public MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq rq) throws BadRequestException {
        this.validar(rq);
        HistoriaMedica historia = this.obtenerHistoria(rq.getHistoriaMedicaId());

        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoriaMedica(historia);
        anotacion.setDescripcion(rq.getDescripcion());
        anotacion.setFecha(rq.getFecha() != null ? rq.getFecha() : LocalDateTime.now());

        anotacionHistoriaRepository.save(anotacion);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotación guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El ID de la anotación es obligatorio para actualizar");
        }

        Optional<AnotacionHistoria> optAnotacion = anotacionHistoriaRepository.findById(rq.getId());
        if (optAnotacion.isEmpty()) {
            throw new BadRequestException("La anotación con ID " + rq.getId() + " no existe");
        }

        HistoriaMedica historia = this.obtenerHistoria(rq.getHistoriaMedicaId());

        AnotacionHistoria anotacion = optAnotacion.get();
        anotacion.setHistoriaMedica(historia);
        anotacion.setDescripcion(rq.getDescripcion());
        anotacion.setFecha(rq.getFecha());

        anotacionHistoriaRepository.save(anotacion);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotación actualizada correctamente");
        return respuesta;
    }

    private void validar(AnotacionHistoriaRq rq) throws BadRequestException {
        if (rq == null || rq.getHistoriaMedicaId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio");
        }
        if (rq.getDescripcion() == null || rq.getDescripcion().trim().isEmpty()) {
            throw new BadRequestException("La descripción de la anotación no puede estar vacía");
        }
    }

    private HistoriaMedica obtenerHistoria(Long id) throws BadRequestException {
        Optional<HistoriaMedica> optHistoria = historiaMedicaRepository.findById(id);
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia médica con ID " + id + " no existe");
        }
        return optHistoria.get();
    }
}