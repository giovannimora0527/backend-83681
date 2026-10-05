package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.UsuarioRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<FormulaMedica> listarFormulasMedicas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    @Override
    public UsuarioRS guardarFormulaMedica(FormulaMedica formula) throws BadRequestException {
        validarFormula(formula);
        formula.setId(null);
        formulaMedicaRepository.save(formula);
        return respuesta("Fórmula médica guardada correctamente");
    }

    @Override
    public UsuarioRS actualizarFormulaMedica(FormulaMedica formula) throws BadRequestException {
        validarFormula(formula);
        if (formula.getId() == null || !formulaMedicaRepository.existsById(formula.getId())) {
            throw new BadRequestException("La fórmula médica que desea actualizar no existe");
        }
        formulaMedicaRepository.save(formula);
        return respuesta("Fórmula médica actualizada correctamente");
    }

    private void validarFormula(FormulaMedica formula) throws BadRequestException {
        if (formula == null || formula.getCitaId() == null || formula.getMedicamentoId() == null
                || formula.getDosis() == null || formula.getDosis().isBlank()) {
            throw new BadRequestException("Cita, medicamento y dosis son obligatorios");
        }
        if (!citaRepository.existsById(formula.getCitaId())) {
            throw new BadRequestException("La cita seleccionada no existe");
        }
        if (!medicamentoRepository.existsById(formula.getMedicamentoId())) {
            throw new BadRequestException("El medicamento seleccionado no existe");
        }
    }

    private UsuarioRS respuesta(String mensaje) {
        UsuarioRS respuesta = new UsuarioRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
