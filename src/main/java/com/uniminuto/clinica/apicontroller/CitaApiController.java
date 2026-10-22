package com.uniminuto.clinica.apicontroller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.CitaVistaRS;
import com.uniminuto.clinica.api.CitaApi;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del recurso cita.
 */
@RestController
public class CitaApiController implements CitaApi {

    @Autowired
    private CitaService citaService;

    @Override
    public ResponseEntity<List<Cita>> listarCitas() throws BadRequestException {
        return ResponseEntity.ok(this.citaService.listarCitas());
    }

    @Override
    public ResponseEntity<List<Cita>> filtrarCitas(String fechaInicial, String fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.filtrarCitas(fechaInicial, fechaFinal));
    }

    @Override
    public ResponseEntity<Cita> consultarCita(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.consultarCita(id));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.guardarCita(citaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarCita(CitaRq citaRq) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.actualizarCita(citaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> reactivarCita(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.citaService.reactivarCita(id));
    }

    @Override
    public ResponseEntity<PaginaRS<CitaVistaRS>> listarCitasPaginadas(
            int pagina, int tamanio, String campo, String direccion,
            Long clienteId, Integer mascotaId, Long medicoId, String estado,
            LocalDate fechaDesde, LocalDate fechaHasta, String texto) throws BadRequestException {
        Filtros.Cita filtros = new Filtros.Cita(clienteId, mascotaId, medicoId, estado, fechaDesde, fechaHasta, texto);
        return ResponseEntity.ok(this.citaService.listarCitasPaginadas(filtros, pagina, tamanio, campo, direccion));
    }
}
