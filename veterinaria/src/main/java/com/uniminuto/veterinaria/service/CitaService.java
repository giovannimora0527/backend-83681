package com.uniminuto.veterinaria.service;

import com.uniminuto.veterinaria.entity.Cita;
import com.uniminuto.veterinaria.repository.CitaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que contiene la logica de negocio para Cita.
 */
@Service
public class CitaService {

    private final CitaRepository repository;

    public CitaService(CitaRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista todas las citas almacenadas.
     * @return Lista general de citas.
     */
    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    /**
     * Filtra citas en un rango de fechas ordenadas por la mas reciente.
     * @param inicio Fecha inicio.
     * @param fin Fecha fin.
     * @return Lista de citas filtradas.
     */
    public List<Cita> filtrarPorFecha(LocalDate inicio, LocalDate fin) {
        return repository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    /**
     * Guarda o actualiza una cita en el sistema.
     * @param cita Objeto cita.
     * @return Cita guardada.
     */
    public Cita guardar(Cita cita) {
        return repository.save(cita);
    }
}