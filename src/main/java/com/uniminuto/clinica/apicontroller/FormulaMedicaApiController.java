
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.entity.CitaMedica;
import java.time.LocalDateTime;

import java.util.List;

@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> getFormulasByOrden(boolean asc)
            throws BadRequestException {
        return ResponseEntity.ok(
                formulaMedicaService.listarFormulasOrdenadas(asc));
    }

    @Autowired
    private FormulaMedicaRepository repository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public ResponseEntity<FormulaMedica> consultarFormula(Long id) {
        return ResponseEntity.ok(repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La fórmula médica no existe.")));
    }

    @Override
    public ResponseEntity<FormulaMedica> actualizarFormula(Long id, FormulaMedica formula) {
        FormulaMedica actual = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La fórmula médica no existe."));
        if (formula.getCitaId() == null || formula.getMedicamentoId() == null || formula.getDosis() == null || formula.getDosis().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cita, medicamento y dosis son obligatorios.");
        CitaMedica cita = citaRepository.findById(formula.getCitaId().longValue()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita indicada no existe."));
        if (cita.getFechaHora() == null || cita.getFechaHora().isAfter(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede asociar una fórmula a una cita futura.");
        if (!"ATENDIDA".equalsIgnoreCase(cita.getEstado())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fórmula solo puede asociarse a una cita atendida.");
        if (!medicamentoRepository.existsById(formula.getMedicamentoId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El medicamento indicado no existe.");
        actual.setCitaId(formula.getCitaId()); actual.setMedicamentoId(formula.getMedicamentoId()); actual.setDosis(formula.getDosis().trim()); actual.setIndicaciones(formula.getIndicaciones()); actual.setFechaActualizacionRegistro(LocalDateTime.now());
        return ResponseEntity.ok(repository.save(actual));
    }

    @Override
    public ResponseEntity<FormulaMedica> crearFormula(FormulaMedica formula) {
        FormulaMedica creada = formulaMedicaService.crearFormula(formula);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
