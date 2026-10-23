
package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<FormulaMedica> listarFormulasOrdenadas(boolean asc) {
        return asc
                ? formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroAsc()
                : formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    @Override
    public FormulaMedica crearFormula(FormulaMedica formula) {

        if (formula == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Los datos de la fórmula son obligatorios");
        }

        if (formula.getId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No envíes el id al crear una fórmula");
        }

        if (formula.getCitaId() == null || formula.getCitaId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Debes indicar una cita válida");
        }

        if (formula.getMedicamentoId() == null || formula.getMedicamentoId() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Debes indicar un medicamento válido");
        }

        if (formula.getDosis() == null || formula.getDosis().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La dosis es obligatoria");
        }

        CitaMedica cita = citaRepository.findById(
                formula.getCitaId().longValue()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "La cita indicada no existe"));

        if (!medicamentoRepository.existsById(formula.getMedicamentoId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El medicamento indicado no existe");
        }

        if (cita.getFechaHora() == null ||
                cita.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede crear una fórmula para una cita futura");
        }

        if (!"ATENDIDA".equalsIgnoreCase(cita.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fórmula solo puede asociarse a una cita atendida");
        }

        if (cita.getMascota() == null || cita.getMedico() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita debe tener mascota y veterinario asociados");
        }

        formula.setFechaCreacionRegistro(LocalDateTime.now());
        formula.setFechaActualizacionRegistro(null);

        return formulaMedicaRepository.save(formula);
    }
}
