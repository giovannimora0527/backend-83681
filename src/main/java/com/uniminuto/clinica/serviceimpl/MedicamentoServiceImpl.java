package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio del catálogo de medicamentos.
 */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<Medicamento> listarMedicamentos() {
        return medicamentoRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public Medicamento buscarMedicamentoPorNombre(String nombre) throws BadRequestException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BadRequestException("El nombre del medicamento no puede estar vacío");
        }
        return medicamentoRepository.findByNombre(nombre.trim())
                .orElseThrow(() -> new BadRequestException("No se encontró el medicamento: " + nombre, HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(medicamentoRq);

        // Paso 2. Evito medicamentos duplicados
        if (medicamentoRepository.findByNombre(medicamentoRq.getNombre().trim()).isPresent()) {
            throw new BadRequestException("Ya existe un medicamento con el nombre " + medicamentoRq.getNombre(), HttpStatus.CONFLICT);
        }

        // Paso 3. Creo la entidad a guardar
        Medicamento medicamento = new Medicamento();
        this.cargarDatos(medicamento, medicamentoRq);
        this.medicamentoRepository.save(medicamento);

        // Paso 4. Retorno la respuesta
        return respuesta("Medicamento guardado correctamente");
    }

    @Override
    public MiRespuestaRS actualizarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(medicamentoRq);
        if (medicamentoRq.getId() == null || medicamentoRq.getId() <= 0) {
            throw new BadRequestException("El ID del medicamento no puede estar vacío ni ser menor o igual a cero");
        }

        // Paso 2. Verifico que exista y que el nombre no pertenezca a otro
        Medicamento medicamento = this.medicamentoRepository.findById(medicamentoRq.getId())
                .orElseThrow(() -> new BadRequestException("El medicamento con ID " + medicamentoRq.getId() + " no existe", HttpStatus.NOT_FOUND));
        if (medicamentoRepository.findByNombre(medicamentoRq.getNombre().trim())
                .filter(otro -> !otro.getId().equals(medicamento.getId())).isPresent()) {
            throw new BadRequestException("Ya existe otro medicamento con el nombre " + medicamentoRq.getNombre(), HttpStatus.CONFLICT);
        }

        // Paso 3. Actualizo los datos
        this.cargarDatos(medicamento, medicamentoRq);
        this.medicamentoRepository.save(medicamento);

        // Paso 4. Retorno la respuesta
        return respuesta("Medicamento actualizado correctamente");
    }

    private void cargarDatos(Medicamento medicamento, MedicamentoRq rq) {
        medicamento.setNombre(rq.getNombre().trim());
        medicamento.setDescripcion(rq.getDescripcion());
        medicamento.setPresentacion(rq.getPresentacion());
        medicamento.setFechaCompra(rq.getFechaCompra());
        medicamento.setFechaVence(rq.getFechaVence());
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(MedicamentoRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (rq.getNombre() == null || rq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del medicamento es obligatorio");
        }
        if (rq.getFechaCompra() == null || rq.getFechaVence() == null) {
            throw new BadRequestException("La fecha de compra y la de vencimiento son obligatorias");
        }
        if (rq.getFechaVence().isBefore(rq.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento debe ser posterior a la fecha de compra");
        }
    }
}
