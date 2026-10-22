package com.uniminuto.clinica.apicontroller;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.AnotacionVistaRS;
import com.uniminuto.clinica.api.AnotacionHistoriaApi;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST del recurso anotación de historia clínica.
 */
@RestController
public class AnotacionHistoriaApiController implements AnotacionHistoriaApi {

    @Autowired
    private AnotacionHistoriaService anotacionHistoriaService;

    @Override
    public ResponseEntity<List<AnotacionHistoria>> listarAnotaciones(Long historiaId) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.listarAnotaciones(historiaId));
    }

    @Override
    public ResponseEntity<List<AnotacionHistoria>> filtrarAnotaciones(Long historiaId, String fechaInicial, String fechaFinal) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.filtrarAnotaciones(historiaId, fechaInicial, fechaFinal));
    }

    @Override
    public ResponseEntity<AnotacionHistoria> consultarAnotacion(Long id) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.consultarAnotacion(id));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.guardarAnotacion(anotacionRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        return ResponseEntity.ok(this.anotacionHistoriaService.actualizarAnotacion(anotacionRq));
    }

    @Override
    public ResponseEntity<PaginaRS<AnotacionVistaRS>> listarAnotacionesPaginadas(
            int pagina, int tamanio, String campo, String direccion,
            Long historiaId, LocalDate fechaDesde, LocalDate fechaHasta, String texto) throws BadRequestException {
        Filtros.Anotacion filtros = new Filtros.Anotacion(historiaId, fechaDesde, fechaHasta, texto);
        return ResponseEntity.ok(this.anotacionHistoriaService.listarAnotacionesPaginadas(filtros, pagina, tamanio, campo, direccion));
    }
}
