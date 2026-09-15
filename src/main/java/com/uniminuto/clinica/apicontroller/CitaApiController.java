package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.UsuarioRS;
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

    /**
     * Servicio encargado de la lógica empresarial de las citas.
     */
    @Autowired
    private CitaService citaService;

    /**
     * Lista todas las citas registradas en el sistema ordenadas por fecha más reciente.
     *
     * @return Respuesta HTTP con la colección de citas.
     * @throws BadRequestException Si ocurre un error en la validación de la solicitud.
     */
    @Override
    public ResponseEntity<List<Cita>> listarCitas() throws BadRequestException {
        return ResponseEntity.ok(this.citaService.filtrarCitas(null, null));
    }

    /**
     * Filtra citas por un rango de fechas recibido en formato ISO-8601 o YYYY-MM-DD.
     *
     * @param fechaInicial Fecha inicial del rango a consultar.
     * @param fechaFinal Fecha final del rango a consultar.
     * @return Respuesta HTTP con las citas filtradas.
     * @throws BadRequestException Si una de las fechas es nula, vacía o tiene formato inválido.
     */
    @Override
    public ResponseEntity<List<Cita>> filtrarCitas(@org.springframework.web.bind.annotation.RequestParam String fechaInicial, @org.springframework.web.bind.annotation.RequestParam String fechaFinal) throws BadRequestException {
        // Ambos parámetros son obligatorios para este endpoint.
        if (fechaInicial == null || fechaInicial.isBlank() || fechaFinal == null || fechaFinal.isBlank()) {
            throw new BadRequestException("fechaInicial y fechaFinal son requeridas para filtrar");
        }

        try {
            // Fecha inicial convertida a LocalDateTime.
            LocalDateTime fi;
            // Fecha final convertida a LocalDateTime.
            LocalDateTime ff;
            try {
                // Primero intenta parsear como LocalDateTime completo.
                fi = LocalDateTime.parse(fechaInicial);
                ff = LocalDateTime.parse(fechaFinal);
            } catch (DateTimeParseException ignored) {
                // Si falla, intenta parsear como fecha únicamente y toma inicio/fin del día.
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

    /**
     * Crea una nueva cita con los datos recibidos en la solicitud.
     *
     * @param citaRq Objeto con la información de la cita a guardar.
     * @return Respuesta HTTP con el resultado de la creación.
     * @throws BadRequestException Si la petición no cumple las validaciones del negocio.
     */
    @Override
    public ResponseEntity<UsuarioRS> guardarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.guardarCita(citaRq));
    }

    /**
     * Actualiza una cita existente con los datos enviados por el cliente.
     *
     * @param citaRq Objeto con los datos actualizados de la cita.
     * @return Respuesta HTTP con el resultado de la actualización.
     * @throws BadRequestException Si la cita no existe o si los datos no son válidos.
     */
    @Override
    public ResponseEntity<UsuarioRS> actualizarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(citaRq));
    }
}
