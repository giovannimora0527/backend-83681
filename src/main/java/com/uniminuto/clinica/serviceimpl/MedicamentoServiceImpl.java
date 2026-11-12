package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<Medicamento> listarMedicamentos() throws BadRequestException {
        return medicamentoRepository.findAll().stream()
                .sorted((m1, m2) -> m1.getNombre().compareToIgnoreCase(m2.getNombre()))
                .toList();
    }

    @Override
    public List<Medicamento> buscarMedicamentos(String termino) {
        String busqueda = termino == null ? "" : termino.trim().toLowerCase(Locale.ROOT);
        return listarMedicamentos().stream()
                .filter(medicamento -> busqueda.isEmpty()
                        || contiene(medicamento.getNombre(), busqueda)
                        || contiene(medicamento.getDescripcion(), busqueda)
                        || contiene(medicamento.getPresentacion(), busqueda))
                .toList();
    }

    @Override
    public MiRespuestaRS crearMedicamento(MedicamentoRq medicamentoRq) {
        validarMedicamento(medicamentoRq);
        String nombre = medicamentoRq.getNombre().trim();
        if (medicamentoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BadRequestException("Ya existe un medicamento con ese nombre.");
        }

        Medicamento medicamento = new Medicamento();
        mapearMedicamento(medicamento, medicamentoRq);
        medicamento.setFechaCreacionRegistro(LocalDateTime.now());
        medicamentoRepository.save(medicamento);
        return respuesta("Medicamento creado correctamente.");
    }

    @Override
    public MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq) {
        validarMedicamento(medicamentoRq);
        if (medicamentoRq.getId() == null || medicamentoRq.getId() <= 0) {
            throw new BadRequestException("El id del medicamento es obligatorio y debe ser mayor a cero.");
        }
        Optional<Medicamento> existente = medicamentoRepository.findById(medicamentoRq.getId());
        if (existente.isEmpty()) {
            throw new BadRequestException("El medicamento no existe.");
        }
        String nombre = medicamentoRq.getNombre().trim();
        if (medicamentoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, medicamentoRq.getId())) {
            throw new BadRequestException("Ya existe otro medicamento con ese nombre.");
        }

        Medicamento medicamento = existente.get();
        mapearMedicamento(medicamento, medicamentoRq);
        medicamento.setFechaModificacionRegistro(LocalDateTime.now());
        medicamentoRepository.save(medicamento);
        return respuesta("Medicamento actualizado correctamente.");
    }

    private void validarMedicamento(MedicamentoRq medicamentoRq) {
        if (medicamentoRq == null) {
            throw new BadRequestException("La información del medicamento es obligatoria.");
        }
        if (medicamentoRq.getNombre() == null || medicamentoRq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del medicamento es obligatorio.");
        }
        if (medicamentoRq.getFechaCompra() == null || medicamentoRq.getFechaVence() == null) {
            throw new BadRequestException("Las fechas de compra y vencimiento son obligatorias.");
        }
        if (medicamentoRq.getFechaVence().isBefore(medicamentoRq.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la fecha de compra.");
        }
    }

    private void mapearMedicamento(Medicamento medicamento, MedicamentoRq solicitud) {
        medicamento.setNombre(solicitud.getNombre().trim());
        medicamento.setDescripcion(limpiar(solicitud.getDescripcion()));
        medicamento.setPresentacion(limpiar(solicitud.getPresentacion()));
        medicamento.setFechaCompra(solicitud.getFechaCompra());
        medicamento.setFechaVence(solicitud.getFechaVence());
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
