package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
    private AnotacionHistoriaRepository anotacionRepository;

    @Override
    public List<AnotacionHistoriaRs> listarAnotaciones(LocalDate fechaInicio, LocalDate fechaFin) throws BadRequestException {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha inicial no puede ser mayor a la final");
        }

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        return anotacionRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin).stream()
                .map(this::aPublico)
                .collect(Collectors.toList());
    }

    @Override
    public AnotacionHistoriaRs crearAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        this.validarAnotacion(anotacionRq, true);

        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoriaId(anotacionRq.getHistoriaId());
        anotacion.setMedicoId(anotacionRq.getMedicoId());
        anotacion.setDescripcion(anotacionRq.getDescripcion().trim());
        anotacion.setFecha(LocalDateTime.now());

        anotacionRepository.save(anotacion);
        return aPublico(anotacion);
    }

    @Override
    public AnotacionHistoriaRs actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        if (anotacionRq == null || anotacionRq.getId() == null) {
            throw new BadRequestException("El ID de la anotación es obligatorio para actualizar");
        }

        Optional<AnotacionHistoria> optAnotacion = this.anotacionRepository.findById(anotacionRq.getId());
        if (optAnotacion.isEmpty()) {
            throw new BadRequestException("Anotación no encontrada");
        }

        AnotacionHistoria anotacion = optAnotacion.get();
        this.validarAnotacion(anotacionRq, false);

        if (anotacionRq.getDescripcion() != null && !anotacionRq.getDescripcion().trim().isEmpty()) {
            anotacion.setDescripcion(anotacionRq.getDescripcion().trim());
        }

        if (anotacionRq.getMedicoId() != null) {
            anotacion.setMedicoId(anotacionRq.getMedicoId());
        }

        anotacionRepository.save(anotacion);
        return aPublico(anotacion);
    }

    private void validarAnotacion(AnotacionHistoriaRq anotacionRq, boolean esCreacion) throws BadRequestException {
        if (anotacionRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (esCreacion && anotacionRq.getHistoriaId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio");
        }
        if (esCreacion && anotacionRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (esCreacion || anotacionRq.getDescripcion() != null) {
            if (anotacionRq.getDescripcion() == null || anotacionRq.getDescripcion().trim().isEmpty()) {
                throw new BadRequestException("La descripción es obligatoria");
            }
        }
    }

    private AnotacionHistoriaRs aPublico(AnotacionHistoria anotacion) {
        AnotacionHistoriaRs rs = new AnotacionHistoriaRs();
        rs.setId(anotacion.getId());
        rs.setHistoriaId(anotacion.getHistoriaId());
        rs.setMedicoId(anotacion.getMedicoId());
        rs.setFecha(anotacion.getFecha());
        rs.setDescripcion(anotacion.getDescripcion());
        return rs;
    }
}
