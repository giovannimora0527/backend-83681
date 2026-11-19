package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaCalendarioRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.ReprogramarCitaRq;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Recibe las solicitudes de agenda y delega las reglas de citas en el servicio. Prepara el Excel
 * del calendario con el total de citas.
 */
@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    /** Consulta las citas del veterinario en las fechas indicadas. */
    @Override
    public List<CitaCalendarioRs> calendario(LocalDateTime inicio, LocalDateTime fin) {
        return citaService.calendario(inicio, fin);
    }

    /** Consulta los datos de la cita. */
    @Override
    public CitaCalendarioRs detalle(Long id) {
        return citaService.detalle(id);
    }

    /** Solicita cambiar el horario de la cita indicada. */
    @Override
    public CitaCalendarioRs reprogramar(Long id, ReprogramarCitaRq solicitud) {
        return citaService.reprogramar(id, solicitud == null ? null : solicitud.fechaHora());
    }

    /** Genera y devuelve el Excel con las citas del período solicitado. */
    @Override
    public ResponseEntity<byte[]> excel(String inicio, String fin) throws IOException {
        var intervalo = intervaloExcel(inicio, fin);
        var citas = citaService.calendario(intervalo.inicio(), intervalo.fin());
        try (var libro = new XSSFWorkbook(); var salida = new ByteArrayOutputStream()) {
            var hoja = libro.createSheet("Citas");
            var encabezado = hoja.createRow(0);
            var titulos = List.of("Cita", "Mascota", "Propietario", "Inicio", "Fin", "Motivo", "Estado");
            for (int i = 0; i < titulos.size(); i++) {
                encabezado.createCell(i).setCellValue(titulos.get(i));
            }
            int indice = 1;
            for (var cita : citas) {
                var fila = hoja.createRow(indice++);
                fila.createCell(0).setCellValue(cita.id().toString());
                fila.createCell(1).setCellValue(cita.mascota());
                fila.createCell(2).setCellValue(cita.propietario());
                fila.createCell(3).setCellValue(cita.inicio().toString());
                fila.createCell(4).setCellValue(cita.fin().toString());
                fila.createCell(5).setCellValue(cita.motivo());
                fila.createCell(6).setCellValue(cita.estado());
            }
            hoja.createRow(indice).createCell(0).setCellValue("Total de citas: " + citas.size());
            libro.write(salida);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=citas.xlsx")
                    .body(salida.toByteArray());
        }
    }

    /** Valida y prepara las fechas para consultar las citas del Excel. */
    private IntervaloExcel intervaloExcel(String inicio, String fin) {
        try {
            if (inicio.length() == 10 && fin.length() == 10) {
                return new IntervaloExcel(LocalDate.parse(inicio).atStartOfDay(),
                        LocalDate.parse(fin).plusDays(1).atStartOfDay());
            }
            return new IntervaloExcel(LocalDateTime.parse(inicio, DateTimeFormatter.ISO_DATE_TIME),
                    LocalDateTime.parse(fin, DateTimeFormatter.ISO_DATE_TIME));
        } catch (DateTimeException ex) {
            throw new BadRequestException("Envía inicio y fin como fechas ISO 8601, ambos con hora o ambos sin hora.");
        }
    }

    /** Contiene las fechas validadas para exportar las citas. */
    private record IntervaloExcel(LocalDateTime inicio, LocalDateTime fin) {
    }
    /** Consulta las citas. */
    @Override
    public ResponseEntity<List<Cita>> listarCitas() throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitas());
    }

    /** Consulta las citas entre la fecha inicial y la final. */
    @Override
    public ResponseEntity<List<Cita>> listarCitasRango(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitasRango(fechaInicio, fechaFin));
    }

    /** Consulta el historial de citas de la mascota indicada. */
    @Override
    public ResponseEntity<List<Cita>> listarCitasPorMascota(Long mascotaId) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitasPorMascota(mascotaId));
    }

    /** Registra la cita. */
    @Override
    public ResponseEntity<MiRespuestaRS> crearCita(CitaRq cita) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.crearCita(cita));
    }

    /** Actualiza la cita. */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq cita) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(cita));
    }
}
