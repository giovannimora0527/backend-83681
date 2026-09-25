package com.uniminuto.veterinaria.serviceimpl;

import com.uniminuto.veterinaria.entity.Cita;
import com.uniminuto.veterinaria.repository.CitaRepository;
import com.uniminuto.veterinaria.service.CitaService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

import java.util.List;

/**
 * Implementación de los servicios de negocio para la gestión de Citas.
 */
@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository repository;

    /**
     * Inyección por constructor del repositorio de Citas.
     *
     * @param repository Instancia de {@link CitaRepository}.
     */
    public CitaServiceImpl(CitaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    @Override
    public List<Cita> filtrarPorFecha(String inicio, String fin) {
        LocalDate fechaInicio = LocalDate.parse(inicio);
        LocalDate fechaFin = LocalDate.parse(fin);
        return repository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin);
    }

    @Override
    public Cita guardarCita(Cita cita) {
        return repository.save(cita);
    }

    @Override
    public Cita actualizarCita(Long id, Cita cita) {
        Cita existente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        existente.setMascota(cita.getMascota());
        existente.setMotivo(cita.getMotivo());
        existente.setFecha(cita.getFecha());
        existente.setEstado(cita.getEstado());
        return repository.save(existente);
    }
}