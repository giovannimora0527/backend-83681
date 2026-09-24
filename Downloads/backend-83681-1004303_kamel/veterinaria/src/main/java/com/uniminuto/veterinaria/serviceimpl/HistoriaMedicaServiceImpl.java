package com.uniminuto.veterinaria.serviceimpl;

import com.uniminuto.veterinaria.entity.HistoriaMedica;
import com.uniminuto.veterinaria.repository.HistoriaMedicaRepository;
import com.uniminuto.veterinaria.service.HistoriaMedicaService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.util.List;

/**
 * Implementación de los servicios de negocio para la gestión de Historias Médicas.
 */
@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    private final HistoriaMedicaRepository repository;

    public HistoriaMedicaServiceImpl(HistoriaMedicaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<HistoriaMedica> listarTodas() {
        return repository.findAll();
    }

    @Override
    public List<HistoriaMedica> filtrarPorFecha(String inicio, String fin) {
        LocalDate fechaInicio = LocalDate.parse(inicio);
        LocalDate fechaFin = LocalDate.parse(fin);
        return repository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }

    @Override
    public HistoriaMedica guardarHistoriaMedica(HistoriaMedica historiaMedica) {
        return repository.save(historiaMedica);
    }

    @Override
    public HistoriaMedica actualizarHistoriaMedica(Long id, HistoriaMedica historiaMedica) {
        HistoriaMedica existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Historia médica no encontrada con ID: " + id));
        existente.setPaciente(historiaMedica.getPaciente());
        existente.setDiagnostico(historiaMedica.getDiagnostico());
        existente.setTratamiento(historiaMedica.getTratamiento());
        existente.setFecha(historiaMedica.getFecha());
        return repository.save(existente);
    }
}