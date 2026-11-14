package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicamentoApi;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class MedicamentoApiController implements MedicamentoApi {

    private final MedicamentoService medicamentoService;

    /** Construye el controlador con el servicio de medicamentos. */
    public MedicamentoApiController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentos() throws BadRequestException {
        return ResponseEntity.ok(medicamentoService.listarMedicamentos());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> buscarMedicamentos(String termino) {
        return ResponseEntity.ok(medicamentoService.buscarMedicamentos(termino));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> crearMedicamento(
            MedicamentoRq medicamentoRq, @AuthenticationPrincipal String usuarioResponsableId) {
        return ResponseEntity.ok(medicamentoService.crearMedicamento(medicamentoRq, usuarioResponsableId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedicamento(
            MedicamentoRq medicamentoRq, @AuthenticationPrincipal String usuarioResponsableId) {
        return ResponseEntity.ok(medicamentoService.actualizarMedicamento(medicamentoRq, usuarioResponsableId));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarDisponibles() {
        return ResponseEntity.ok(medicamentoService.listarDisponibles());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarAgotados() {
        return ResponseEntity.ok(medicamentoService.listarAgotados());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarStockBajo() {
        return ResponseEntity.ok(medicamentoService.listarStockBajo());
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<List<Medicamento>> listarProximosAVencer(int dias) {
        return ResponseEntity.ok(medicamentoService.listarProximosAVencer(dias));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(Long medicamentoId, Integer cantidadRequerida) {
        return ResponseEntity.ok(medicamentoService.consultarDisponibilidad(medicamentoId, cantidadRequerida));
    }

}
