package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ReporteRs;
import lombok.RequiredArgsConstructor;
import com.uniminuto.clinica.repository.ReporteFinancieroRepository;
import com.uniminuto.clinica.service.ReporteFinancieroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Valida el rango y la agrupación y compone el reporte de facturación, pagos, devoluciones y
 * recaudo neto.
 */
@Service
@RequiredArgsConstructor
public class ReporteFinancieroServiceImpl implements ReporteFinancieroService {

    private final ReporteFinancieroRepository reportes;

    /** Valida las fechas y la agrupación antes de calcular facturación, recaudo y devoluciones. */
    @Override
    @Transactional(readOnly = true)
    public ReporteRs consultar(LocalDate inicio, LocalDate fin, String periodo, Long medico, Long especialidad) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new BadRequestException("Indica un rango de fechas válido.");
        }
        String formato = switch (periodo) {
            case "dia" -> "%Y-%m-%d";
            case "mes" -> "%Y-%m";
            case "anio" -> "%Y";
            default -> throw new BadRequestException("El período debe ser dia, mes o anio.");
        };
        var filas = reportes.consultar(inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay(), formato,
                medico, especialidad);

        return new ReporteRs("Gestión financiera del " + inicio + " al " + fin,
                LocalDateTime.now(ZoneId.of("America/Bogota")),
                List.of("Concepto", "Grupo", "Facturas", "Pagadas", "Pendientes", "Parciales", "Anuladas",
                        "Importe", "IVA", "Descuentos", "No requieren pago"), filas);
    }
}
