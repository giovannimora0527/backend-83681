package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicoApi;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CuentaMedicoRq;
import com.uniminuto.clinica.models.CuentaMedicoRs;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Recibe las consultas del directorio médico y las solicitudes para vincular o desvincular cuentas
 * veterinarias.
 */
@RestController
public class MedicoApiController implements MedicoApi {

    @Autowired
    private MedicoService medicoService;

    /** Consulta las cuentas que pueden asociarse a un veterinario. */
    @Override
    public List<CuentaMedicoRs> listarCuentasDisponibles() {
        return medicoService.listarCuentasDisponibles();
    }

    /** Consulta la cuenta de acceso asociada al veterinario. */
    @Override
    public ResponseEntity<CuentaMedicoRs> consultarCuenta(Long medicoId) {
        return medicoService.consultarCuenta(medicoId).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** Relaciona la cuenta seleccionada con el veterinario. */
    @Override
    public CuentaMedicoRs vincularCuenta(Long medicoId, CuentaMedicoRq solicitud) {
        return medicoService.vincularCuenta(medicoId, solicitud == null ? null : solicitud.usuarioId());
    }

    /** Desasocia la cuenta de acceso del veterinario indicado. */
    @Override
    public ResponseEntity<Void> desvincularCuenta(Long medicoId) {
        medicoService.desvincularCuenta(medicoId);
        return ResponseEntity.noContent().build();
    }

    /** Consulta los veterinarios que pueden atender en el horario indicado. */
    @Override
    public ResponseEntity<List<Medico>> listarMedicosDisponibles(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                            Long citaId, Long mascotaId)
            throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.listarMedicosDisponiblesPorFecha(fechaInicio, fechaFin, citaId, mascotaId));
    }

    /** Consulta todos los registros del catálogo. */
    @Override
    public ResponseEntity<List<Medico>> listarTodos() throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.listarMedicos());
    }

    /** Busca los veterinarios. */
    @Override
    public ResponseEntity<List<Medico>> buscarMedicos(String termino) {
        return ResponseEntity.ok(this.medicoService.buscarMedicos(termino));
    }

    /** Registra el veterinario. */
    @Override
    public ResponseEntity<MiRespuestaRS> crearMedico(MedicoRq medicoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.guardarMedico(medicoRq));
    }

    /** Actualiza el veterinario. */
    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedico(MedicoRq medicoRq) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.actualizarMedico(medicoRq));
    }
}
