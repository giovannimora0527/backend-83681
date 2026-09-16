package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de gestión de citas médicas.
 */
@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cita> filtrarPorFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return citaRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cita guardarCita(Cita cita) {
        return citaRepository.save(cita);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cita actualizarCita(Long id, Cita cita) {
        if (citaRepository.existsById(id)) {
            cita.setId(id);
            return citaRepository.save(cita);
        }
        throw new RuntimeException("Cita no encontrada con el id: " + id);
    }
}