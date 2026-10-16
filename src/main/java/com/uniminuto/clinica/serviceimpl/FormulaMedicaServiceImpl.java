package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<FormulaMedica> findAllFormulas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    @Override
    public Optional<FormulaMedica> buscarPorId(Long id) {
        return id == null ? Optional.empty() : formulaMedicaRepository.findById(id);
    }

    @Override
    public MiRespuestaRS guardar(FormulaMedicaRq rq) {
        validar(rq);

        Cita cita = buscarCita(rq.getCitaId());
        Medicamento medicamento = buscarMedicamento(rq.getMedicamentoId());
        validarFecha(rq.getFechaEmision(), cita);

        FormulaMedica formula = new FormulaMedica();
        asignar(formula, rq, cita, medicamento);
        formula.setFechaCreacionRegistro(LocalDateTime.now());
        formulaMedicaRepository.save(formula);

        return respuesta("La fórmula médica se registró correctamente.");
    }

    @Override
    public MiRespuestaRS actualizar(FormulaMedicaRq rq) {
        validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El identificador de la fórmula es obligatorio.");
        }

        FormulaMedica formula = formulaMedicaRepository.findById(rq.getId())
                .orElseThrow(() -> new BadRequestException("La fórmula médica no existe."));

        Cita cita = buscarCita(rq.getCitaId());
        Medicamento medicamento = buscarMedicamento(rq.getMedicamentoId());
        validarFecha(rq.getFechaEmision(), cita);

        asignar(formula, rq, cita, medicamento);
        formula.setFechaActualizacionRegistro(LocalDateTime.now());
        formulaMedicaRepository.save(formula);

        return respuesta("La fórmula médica se actualizó correctamente.");
    }

    private void validar(FormulaMedicaRq rq) {
        if (rq == null || rq.getCitaId() == null || rq.getMedicamentoId() == null
                || !textoValido(rq.getDosis()) || !textoValido(rq.getFrecuencia())
                || rq.getDuracionTratamiento() == null
                || rq.getDuracionTratamiento().trim().isEmpty()
                || rq.getFechaEmision() == null) {
            throw new BadRequestException("Completa los campos obligatorios de la fórmula.");
        }
    }

    private boolean textoValido(String texto) {
        return texto != null && !texto.isBlank();
    }

    private Cita buscarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La cita no existe."));

        if (cita.getCliente() == null || cita.getMascota() == null
                || cita.getMedico() == null || cita.getMascota().getCliente() == null
                || !Objects.equals(cita.getCliente().getId(),
                cita.getMascota().getCliente().getId())) {
            throw new BadRequestException("La cita no tiene relaciones válidas.");
        }
        return cita;
    }

    private Medicamento buscarMedicamento(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El medicamento no existe."));
    }

    private void validarFecha(LocalDateTime emision, Cita cita) {
        if (emision.isBefore(cita.getFechaHora())) {
            throw new BadRequestException("La emisión no puede ser anterior a la cita.");
        }
    }

    private void asignar(FormulaMedica formula, FormulaMedicaRq rq,
                         Cita cita, Medicamento medicamento) {
        formula.setCita(cita);
        formula.setMedicamento(medicamento);
        formula.setDosis(rq.getDosis().trim());
        formula.setFrecuencia(rq.getFrecuencia().trim());
        formula.setDuracionTratamiento(String.valueOf(rq.getDuracionTratamiento()));
        formula.setIndicaciones(rq.getIndicaciones());
        formula.setFechaEmision(rq.getFechaEmision());
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}