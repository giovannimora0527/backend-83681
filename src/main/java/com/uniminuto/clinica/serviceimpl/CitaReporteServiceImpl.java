package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaReporteService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

@Service
public class CitaReporteServiceImpl implements CitaReporteService {
    private final CitaRepository citaRepository;

    public CitaReporteServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public ByteArrayOutputStream exportarExcel(Medico medico, LocalDateTime inicio, LocalDateTime fin) {
        var citas = citaRepository.findByMedicoAndRange(medico, inicio, fin);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet resumen = workbook.createSheet("Resumen");
            Map<EstadoCita, Integer> conteos = new EnumMap<>(EstadoCita.class);
            for (EstadoCita estado : EstadoCita.values()) conteos.put(estado, 0);
            for (Cita cita : citas) conteos.merge(cita.getEstado(), 1, Integer::sum);

            resumen.createRow(0).createCell(0).setCellValue("Estado");
            resumen.getRow(0).createCell(1).setCellValue("Cantidad");
            int fila = 1;
            for (EstadoCita estado : EstadoCita.values()) {
                Row r = resumen.createRow(fila++);
                r.createCell(0).setCellValue(estado.name());
                r.createCell(1).setCellValue(conteos.get(estado));
            }

            Sheet detalle = workbook.createSheet("Citas");
            String[] headers = {"ID", "Propietario", "Mascota", "Motivo", "Estado", "Inicio", "Fin"};
            Row header = detalle.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            fila = 1;
            for (Cita c : citas) {
                var p = c.getMascota().getCliente();
                Row r = detalle.createRow(fila++);
                r.createCell(0).setCellValue(c.getId());
                r.createCell(1).setCellValue(p.getNombres() + " " + p.getApellidos());
                r.createCell(2).setCellValue(c.getMascota().getNombreMascota());
                r.createCell(3).setCellValue(c.getMotivoConsulta());
                r.createCell(4).setCellValue(c.getEstado().name());
                r.createCell(5).setCellValue(c.getFechaHoraInicio().format(formato));
                r.createCell(6).setCellValue(c.getFechaHoraFin().format(formato));
            }
            for (int i = 0; i < headers.length; i++) detalle.autoSizeColumn(i);
            workbook.write(output);
            return new ByteArrayOutputStream() {{ write(output.toByteArray()); }};
        } catch (Exception ex) {
            throw new RuntimeException("No fue posible generar el reporte Excel", ex);
        }
    }
}
