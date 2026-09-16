package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de la lógica de negocio para AnotacionHistoria.
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    private final AnotacionHistoriaRepository anotacionRepository;

    @Autowired
    public AnotacionHistoriaServiceImpl(AnotacionHistoriaRepository anotacionRepository) {
        this.anotacionRepository = anotacionRepository;
    }

    @Override
    public AnotacionHistoria crearAnotacion(AnotacionHistoria anotacion) {
        if (anotacion.getFecha() == null) {
            anotacion.setFecha(LocalDateTime.now());
        }
        return anotacionRepository.save(anotacion);
    }

    @Override
    public AnotacionHistoria actualizarAnotacion(Long id, AnotacionHistoria anotacion) {
        AnotacionHistoria existente = anotacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anotación de historia no encontrada con ID: " + id));

        existente.setDescripcion(anotacion.getDescripcion());
        if (anotacion.getFecha() != null) {
            existente.setFecha(anotacion.getFecha());
        }
        if (anotacion.getHistoriaMedica() != null) {
            existente.setHistoriaMedica(anotacion.getHistoriaMedica());
        }

        return anotacionRepository.save(existente);
    }

    @Override
    public List<AnotacionHistoria> listarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return anotacionRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }
}