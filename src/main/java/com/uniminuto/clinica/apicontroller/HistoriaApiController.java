package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.HistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import com.uniminuto.clinica.service.HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controlador REST que implementa los endpoints definidos en HistoriaApi.
 */
@RestController
public class HistoriaApiController implements HistoriaApi {

    /** Servicio para operaciones sobre historias. */
    @Autowired
    private HistoriaService historiaService;

    /** Servicio para operaciones sobre anotaciones de historia. */
    @Autowired
    private AnotacionHistoriaService anotacionService;

    /**
     * Obtiene todas las historias (delegado al servicio).
     * @return ResponseEntity con lista de historias
     */
    @Override
    public ResponseEntity<List<HistoriaMedica>> listarHistorias() throws BadRequestException {
        return ResponseEntity.ok(this.historiaService.filtrarHistorias(null, null));
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
    public ResponseEntity<List<HistoriaMedica>> filtrarHistorias(String fechaInicial, String fechaFinal) throws BadRequestException {
        if (fechaInicial == null || fechaInicial.isBlank() || fechaFinal == null || fechaFinal.isBlank()) {
            throw new BadRequestException("fechaInicial y fechaFinal son requeridas para filtrar");
        }

        try {
            LocalDateTime fi;
            LocalDateTime ff;
            try {
                fi = LocalDateTime.parse(fechaInicial);
                ff = LocalDateTime.parse(fechaFinal);
            } catch (DateTimeParseException ignored) {
                try {
                    LocalDate di = LocalDate.parse(fechaInicial);
                    LocalDate df = LocalDate.parse(fechaFinal);
                    fi = di.atStartOfDay();
                    ff = df.atTime(LocalTime.MAX);
                } catch (DateTimeParseException ex2) {
                    throw new BadRequestException("Formato de fecha inválido. Use yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss");
                }
            }

            return ResponseEntity.ok(this.historiaService.filtrarHistorias(fi, ff));
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Formato de fecha inválido. Use yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss");
        }
    }

    /**
     * Crea una anotación delegando en el servicio de anotaciones.
     */
    @Override
    public ResponseEntity<UsuarioRS> guardarAnotacion(AnotacionHistoriaRq rq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionService.guardarAnotacion(rq));
    }

    /**
     * Actualiza una anotación delegando en el servicio de anotaciones.
     */
    @Override
    public ResponseEntity<UsuarioRS> actualizarAnotacion(AnotacionHistoriaRq rq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionService.actualizarAnotacion(rq));
    }

    /**
     * Lista anotaciones de una historia.
     */
    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionService.listarAnotacionesByHistoria(historiaId));
    }
}
