package com.uniminuto.veterinaria.service;

import com.uniminuto.veterinaria.entity.HistoriaMedica;
import com.uniminuto.veterinaria.repository.HistoriaMedicaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que contiene la logica de negocio para HistoriaMedica.
 */
@Service
public class HistoriaMedicaService {

    private final HistoriaMedicaRepository repository;

    public HistoriaMedicaService(HistoriaMedicaRepository repository) {
        this.repository = repository;
    }

    /**
     * Guarda o actualiza una historia medica.
     * @param historia Objeto historia medica.
     * @return Historia medica guardada.
     */
    public HistoriaMedica guardar(HistoriaMedica historia) {
        return repository.save(historia);
    }

    /**
     * Filtra historias medicas entre dos fechas de la mas reciente a la antigua.
     * @param inicio Fecha inicial.
     * @param fin Fecha final.
     * @return Lista de historias medicas.
     */
    public List<HistoriaMedica> filtrarPorFecha(LocalDate inicio, LocalDate fin) {
        return repository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }
}