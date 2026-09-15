package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de historias médicas.
 */
@Service
public class HistoriaServiceImpl implements HistoriaService {

    /** Repositorio para acceder a las historias en BD. */
    @Autowired
    private HistoriaMedicaRepository historiaRepo;

    /**
     * Filtra historias por rango de fechas delegando en el repositorio.
     * Si inicio o fin son nulos, devuelve todas las historias ordenadas por fecha descendente.
     * @param inicio fecha inicial o null
     * @param fin fecha final o null
     * @return lista de historias
     */
    @Override
    public List<HistoriaMedica> filtrarHistorias(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return this.historiaRepo.findAllByOrderByFechaCreacionDesc();
        }
        return this.historiaRepo.findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(inicio, fin);
    }
}
