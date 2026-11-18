package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.repository.ReporteMedicoRepository;
import com.uniminuto.clinica.service.ReporteMedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Valida el rango y compone el reporte de agenda y consultas realizadas para la vista previa y la
 * descarga.
 */
@Service
@RequiredArgsConstructor
public class ReporteMedicoServiceImpl implements ReporteMedicoService {

    private final ReporteMedicoRepository reportes;

    @Override
    @Transactional(readOnly = true)
    public ReporteRs consultar(LocalDate inicio, LocalDate fin, Long medicoId, Long especializacionId) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new BadRequestException("Indica un rango de fechas válido.");
        }

        var filas = reportes.consultar(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(),
                medicoId, especializacionId);
        return new ReporteRs("Gestión médica del " + inicio + " al " + fin,
                LocalDateTime.now(ZoneId.of("America/Bogota")),
                List.of("ID médico", "Veterinario", "ID especialidad", "Especialidad", "Citas",
                        "Atendidas", "Canceladas", "Pendientes", "Mascotas atendidas",
                        "Confirmadas", "No asistidas", "Consultas realizadas"), filas);
    }
}
