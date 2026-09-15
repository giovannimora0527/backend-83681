package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    @Override
    public ResponseEntity<List<Cita>> listarCitas() throws BadRequestException {
        return ResponseEntity.ok(this.citaService.filtrarCitas(null, null));
    }

    @Override
    public ResponseEntity<List<Cita>> filtrarCitas(@org.springframework.web.bind.annotation.RequestParam String fechaInicial, @org.springframework.web.bind.annotation.RequestParam String fechaFinal) throws BadRequestException {
        // Ambos parámetros son obligatorios para este endpoint
        if (fechaInicial == null || fechaInicial.isBlank() || fechaFinal == null || fechaFinal.isBlank()) {
            throw new BadRequestException("fechaInicial y fechaFinal son requeridas para filtrar");
        }

        try {
            LocalDateTime fi;
            LocalDateTime ff;
            try {
                // intentamos parsear como LocalDateTime completo
                fi = LocalDateTime.parse(fechaInicial);
                ff = LocalDateTime.parse(fechaFinal);
            } catch (DateTimeParseException ignored) {
                // intentamos parsear solo la fecha (yyyy-MM-dd) y usamos inicio/fin del día
                try {
                    LocalDate di = LocalDate.parse(fechaInicial);
                    LocalDate df = LocalDate.parse(fechaFinal);
                    fi = di.atStartOfDay();
                    ff = df.atTime(LocalTime.MAX);
                } catch (DateTimeParseException ex2) {
                    throw new BadRequestException("Formato de fecha inválido. Use yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss");
                }
            }

            return ResponseEntity.ok(this.citaService.filtrarCitas(fi, ff));
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Formato de fecha inválido. Use yyyy-MM-dd o yyyy-MM-ddTHH:mm:ss");
        }
    }
}
