package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.Historia_MedicaApi;
import com.uniminuto.clinica.entity.Historia_Medica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Historia_MedicaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.service.Historia_MedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;

/**
 * Controlador REST que implementa los endpoints definidos en HistoriaApi.
 */
@RestController
public class Historia_MedicaMedicaApiController implements Historia_MedicaApi {

    /** Servicio para operaciones sobre historias. */
    @Autowired
    private Historia_MedicaService historiaMedicaService;

    /**
     * Obtiene todas las historias (delegado al servicio).
     * @return ResponseEntity con lista de historias
     */
    @Override
    public ResponseEntity<List<Historia_Medica>> listarHistorias() throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.filtrarHistorias(null, null));
    }

    /**
     * Filtra historias por rango, acepta formatos ISO datetime o yyyy-MM-dd.
     * Valida parámetros y transforma fechas a LocalDateTime.
     * @param fechaInicial fecha inicial en texto
     * @param fechaFinal fecha final en texto
     * @return ResponseEntity con lista de historias filtradas
     * @throws BadRequestException si parámetros inválidos o formato incorrecto
     */
    @Override
    public ResponseEntity<List<Historia_Medica>> filtrarHistorias(String fechaInicial, String fechaFinal) throws BadRequestException {
        if (fechaInicial == null || fechaInicial.isBlank() || fechaFinal == null || fechaFinal.isBlank()) {
            throw new BadRequestException("fechaInicial y fechaFinal son requeridas para filtrar");
        }

        LocalDateTime fi = parseFecha(fechaInicial, false);
        LocalDateTime ff = parseFecha(fechaFinal, true);
        return ResponseEntity.ok(this.historiaMedicaService.filtrarHistorias(fi, ff));
    }

    /**
     * Guarda una historia médica.
     *
     * @param historiaMedicaRq datos de la historia
     * @return respuesta HTTP con el resultado
     */
    @Override
    public ResponseEntity<UsuarioRS> guardarHistoria(Historia_MedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.guardarHistoria(historiaMedicaRq));
    }

    /**
     * Actualiza una historia médica.
     *
     * @param historiaMedicaRq datos actualizados
     * @return respuesta HTTP con el resultado
     */
    @Override
    public ResponseEntity<UsuarioRS> actualizarHistoria(Historia_MedicaRq historiaMedicaRq) throws BadRequestException {
        return ResponseEntity.ok(this.historiaMedicaService.actualizarHistoria(historiaMedicaRq));
    }

    private LocalDateTime parseFecha(String valor, boolean esFechaFinal) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                .toFormatter();

        try {
            TemporalAccessor parsed = formatter.parseBest(valor, LocalDateTime::from, LocalDate::from);
            if (parsed instanceof LocalDateTime) {
                return (LocalDateTime) parsed;
            }
            LocalDate fecha = LocalDate.from(parsed);
            return esFechaFinal ? fecha.atTime(LocalTime.MAX) : fecha.atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Formato de fecha inválido. Use yyyy-MM-dd, yyyy-MM-dd HH:mm:ss o yyyy-MM-ddTHH:mm:ss");
        }
    }
}
