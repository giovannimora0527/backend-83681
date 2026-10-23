package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.MedicoApi;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MedicoApiController implements MedicoApi {

    private final MedicoService medicoService;

    public MedicoApiController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @Override
    public ResponseEntity<MiRespuestaRS> guardarMedico(MedicoRq medicoRq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoService.guardarMedico(medicoRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarMedico(MedicoRq medicoRq) {
        return ResponseEntity.ok(medicoService.actualizarMedico(medicoRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> eliminarMedico(Long id) {
        return ResponseEntity.ok(medicoService.eliminarMedico(id));
    }

    @Override
    public ResponseEntity<List<Medico>> getMedicos() throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.listarMedicos());
    }

    @Override
    public ResponseEntity<Medico> buscarMedico(String tipoDocumento, String numeroDocumento) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.buscarMedico(tipoDocumento, numeroDocumento));
    }

    @Override
    public ResponseEntity<Medico> buscarMedicoPorRegistroProfesional(String registroProfesional) throws BadRequestException {
        return ResponseEntity.ok(this.medicoService.buscarMedicoPorRegistroProfesional(registroProfesional));
    }
}
