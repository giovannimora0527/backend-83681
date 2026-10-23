package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;

    public MedicamentoServiceImpl(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    @Override
    public List<Medicamento> listarMedicamentos() {
        return medicamentoRepository.findAll();
    }

    @Override
    @Transactional
    public MiRespuestaRS crearMedicamento(MedicamentoRq rq) {
        validar(rq);
        if (medicamentoRepository.existsByNombreIgnoreCaseAndPresentacionIgnoreCase(
                rq.getNombre().trim(), rq.getPresentacion().trim())) {
            throw new ConflictException("Ya existe ese medicamento con la presentación indicada.");
        }

        Medicamento medicamento = new Medicamento();
        aplicarCambios(medicamento, rq);
        medicamento.setFechaCreacion(LocalDateTime.now());
        medicamento = medicamentoRepository.save(medicamento);
        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Medicamento creado correctamente.",
                medicamento.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarMedicamento(MedicamentoRq rq) {
        validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El identificador del medicamento es obligatorio.");
        }
        Medicamento medicamento = medicamentoRepository.findById(rq.getId())
                .orElseThrow(() -> new NotFoundException("El medicamento seleccionado no existe."));
        if (medicamentoRepository.existsByNombreIgnoreCaseAndPresentacionIgnoreCaseAndIdNot(
                rq.getNombre().trim(), rq.getPresentacion().trim(), rq.getId())) {
            throw new ConflictException("Ya existe otro medicamento con esa presentación.");
        }

        aplicarCambios(medicamento, rq);
        medicamento.setFechaModificacion(LocalDateTime.now());
        medicamentoRepository.save(medicamento);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Medicamento actualizado correctamente.",
                medicamento.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS eliminarMedicamento(Long id) {
        Medicamento medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El medicamento seleccionado no existe."));
        medicamentoRepository.delete(medicamento);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Medicamento eliminado correctamente.", id);
    }

    private void validar(MedicamentoRq rq) {
        if (rq == null || vacio(rq.getNombre()) || vacio(rq.getPresentacion())) {
            throw new BadRequestException("El nombre y la presentación del medicamento son obligatorios.");
        }
        if (rq.getNombre().trim().length() > 100 || rq.getPresentacion().trim().length() > 80) {
            throw new BadRequestException("El nombre admite hasta 100 caracteres y la presentación hasta 80.");
        }
    }

    private void aplicarCambios(Medicamento medicamento, MedicamentoRq rq) {
        medicamento.setNombre(rq.getNombre().trim());
        medicamento.setPresentacion(rq.getPresentacion().trim());
        medicamento.setDescripcion(rq.getDescripcion() == null || rq.getDescripcion().isBlank()
                ? null : rq.getDescripcion().trim());
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
