package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public List<Cita> filtrarCitas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        // Si no vienen fechas, retornar todas ordenadas de la más reciente a la más antigua
        if (fechaInicial == null && fechaFinal == null) {
            return this.citaRepository.findAllByOrderByFechaHoraDesc();
        }

        // Para filtrar se requieren ambas fechas
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("Para filtrar es necesario suministrar fechaInicial y fechaFinal");
        }

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }

        return this.citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicial, fechaFinal);
    }
}
