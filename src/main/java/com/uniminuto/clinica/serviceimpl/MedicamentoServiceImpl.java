package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.LoteMedicamentoRq;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<Medicamento> listarMedicamentos() throws BadRequestException {
        return medicamentoRepository.findAll().stream()
                .sorted(Comparator.comparing(Medicamento::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<Medicamento> consultarDisponiblesEnInventario() throws BadRequestException {
        return medicamentoRepository.findByCantidadActualGreaterThan(0).stream()
                .sorted(Comparator.comparing(Medicamento::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    @Transactional
    public MiRespuestaRS registrarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        Medicamento medicamento = mapearEntidad(medicamentoRq);
        validarMedicamento(medicamento, true);

        if (medicamentoRepository.existsByNombre(medicamento.getNombre().trim())) {
            throw new BadRequestException("Ya existe un producto con ese nombre.");
        }

        if (medicamentoRepository.existsByCodigoAlfanumerico(medicamento.getCodigoAlfanumerico().trim())) {
            throw new BadRequestException("Ya existe un producto con ese código alfanumérico.");
        }

        if (medicamento.getLotes() != null && !medicamento.getLotes().isEmpty()) {
            int totalCantidadLotes = medicamento.getLotes().stream().mapToInt(LoteMedicamento::getCantidad).sum();
            medicamento.setCantidadActual(totalCantidadLotes);
        }

        medicamentoRepository.save(medicamento);
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Producto registrado correctamente.");
        return respuesta;
    }

    @Override
    @Transactional
    public MiRespuestaRS editarMedicamento(MedicamentoRq medicamentoRq) throws BadRequestException {
        if (medicamentoRq == null || medicamentoRq.getId() == null) {
            throw new BadRequestException("El ID del producto es obligatorio para editar.");
        }

        Medicamento existente = medicamentoRepository.findById(medicamentoRq.getId())
                .orElseThrow(() -> new BadRequestException("El producto no existe en el inventario."));

        Medicamento actualizado = mapearEntidad(medicamentoRq);
        validarMedicamento(actualizado, false);

        if (!Objects.equals(existente.getNombre(), actualizado.getNombre())
                && medicamentoRepository.existsByNombre(actualizado.getNombre().trim())) {
            throw new BadRequestException("Ya existe un producto con ese nombre.");
        }

        if (!Objects.equals(existente.getCodigoAlfanumerico(), actualizado.getCodigoAlfanumerico())
                && medicamentoRepository.existsByCodigoAlfanumerico(actualizado.getCodigoAlfanumerico().trim())) {
            throw new BadRequestException("Ya existe un producto con ese código alfanumérico.");
        }

        existente.setNombre(actualizado.getNombre().trim());
        existente.setDescripcion(actualizado.getDescripcion());
        existente.setPresentacion(actualizado.getPresentacion());
        existente.setCodigoAlfanumerico(actualizado.getCodigoAlfanumerico().trim());
        existente.setPrecioCompra(actualizado.getPrecioCompra());
        existente.setPrecioVenta(actualizado.getPrecioVenta());
        existente.setStockMinimo(actualizado.getStockMinimo());
        existente.setFechaIngresoInventario(actualizado.getFechaIngresoInventario());
        existente.setFechaProximoPedido(actualizado.getFechaProximoPedido());
        existente.setRegistroInvima(actualizado.getRegistroInvima() != null ? actualizado.getRegistroInvima().trim() : null);
        existente.setFechaCompra(actualizado.getFechaCompra());
        existente.setFechaVence(actualizado.getFechaVence());

        if (actualizado.getLotes() != null && !actualizado.getLotes().isEmpty()) {
            existente.getLotes().clear();
            for (LoteMedicamento lote : actualizado.getLotes()) {
                lote.setMedicamento(existente);
                existente.getLotes().add(lote);
            }
            existente.setCantidadActual(existente.getLotes().stream().mapToInt(LoteMedicamento::getCantidad).sum());
        } else {
            existente.setCantidadActual(actualizado.getCantidadActual());
        }

        medicamentoRepository.save(existente);
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Producto actualizado correctamente.");
        return respuesta;
    }

    @Override
    public Map<String, Object> consultarEstadoInventario() throws BadRequestException {
        Map<String, Object> estado = new HashMap<>();
        estado.put("agotados", medicamentoRepository.findProductosAgotados());
        estado.put("stockBajo", medicamentoRepository.findProductosConStockBajo());
        estado.put("proximosAVencer", medicamentoRepository.findProductosProximosAVencer(
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        ));
        estado.put("totalProductos", medicamentoRepository.count());
        return estado;
    }

    private Medicamento mapearEntidad(MedicamentoRq medicamentoRq) throws BadRequestException {
        if (medicamentoRq == null) {
            throw new BadRequestException("El objeto del producto no puede ser nulo.");
        }

        Medicamento medicamento = new Medicamento();
        medicamento.setId(medicamentoRq.getId());
        medicamento.setNombre(medicamentoRq.getNombre());
        medicamento.setDescripcion(medicamentoRq.getDescripcion());
        medicamento.setPresentacion(medicamentoRq.getPresentacion());
        medicamento.setCodigoAlfanumerico(medicamentoRq.getCodigoAlfanumerico());
        medicamento.setPrecioCompra(medicamentoRq.getPrecioCompra());
        medicamento.setPrecioVenta(medicamentoRq.getPrecioVenta());
        medicamento.setCantidadActual(medicamentoRq.getCantidadActual());
        medicamento.setStockMinimo(medicamentoRq.getStockMinimo());
        medicamento.setFechaIngresoInventario(medicamentoRq.getFechaIngresoInventario());
        medicamento.setFechaProximoPedido(medicamentoRq.getFechaProximoPedido());
        medicamento.setRegistroInvima(medicamentoRq.getRegistroInvima());
        medicamento.setFechaCompra(medicamentoRq.getFechaCompra());
        medicamento.setFechaVence(medicamentoRq.getFechaVence());

        if (medicamentoRq.getLotes() != null) {
            List<LoteMedicamento> lotes = medicamentoRq.getLotes().stream()
                    .map(this::mapearLote)
                    .toList();
            medicamento.setLotes(lotes);
            if (medicamento.getCantidadActual() == null) {
                medicamento.setCantidadActual(lotes.stream().mapToInt(LoteMedicamento::getCantidad).sum());
            }
        }

        return medicamento;
    }

    private LoteMedicamento mapearLote(LoteMedicamentoRq loteRq) {
        if (loteRq == null) {
            return null;
        }
        LoteMedicamento lote = new LoteMedicamento();
        lote.setId(loteRq.getId());
        lote.setNumeroLote(loteRq.getNumeroLote());
        lote.setCantidad(loteRq.getCantidad());
        lote.setPrecioCompraLote(loteRq.getPrecioCompraLote());
        lote.setFechaIngresoLote(loteRq.getFechaIngresoLote());
        lote.setFechaVencimientoLote(loteRq.getFechaVencimientoLote());
        return lote;
    }

    private void validarMedicamento(Medicamento medicamento, boolean esNuevo) throws BadRequestException {
        if (medicamento == null) {
            throw new BadRequestException("Los datos del producto son obligatorios.");
        }
        if (medicamento.getNombre() == null || medicamento.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del producto es obligatorio.");
        }
        if (medicamento.getCodigoAlfanumerico() == null || medicamento.getCodigoAlfanumerico().isBlank()) {
            throw new BadRequestException("El código alfanumérico es obligatorio.");
        }
        if (!medicamento.getCodigoAlfanumerico().matches("[A-Za-z0-9]{8}")) {
            throw new BadRequestException("El código alfanumérico debe tener exactamente 8 caracteres alfanuméricos.");
        }
        if (medicamento.getPrecioCompra() == null || medicamento.getPrecioCompra() < 0) {
            throw new BadRequestException("El precio de compra es obligatorio y no puede ser negativo.");
        }
        if (medicamento.getPrecioVenta() == null || medicamento.getPrecioVenta() < 0) {
            throw new BadRequestException("El precio de venta es obligatorio y no puede ser negativo.");
        }
        if (medicamento.getCantidadActual() == null || medicamento.getCantidadActual() < 0) {
            throw new BadRequestException("La cantidad actual es obligatoria y no puede ser negativa.");
        }
        if (medicamento.getStockMinimo() == null || medicamento.getStockMinimo() < 0) {
            throw new BadRequestException("El stock mínimo es obligatorio y no puede ser negativo.");
        }
        if (medicamento.getFechaIngresoInventario() == null) {
            throw new BadRequestException("La fecha de ingreso al inventario es obligatoria.");
        }
        if (medicamento.getFechaCompra() == null) {
            throw new BadRequestException("La fecha de compra es obligatoria.");
        }
        if (medicamento.getFechaVence() == null) {
            throw new BadRequestException("La fecha de vencimiento es obligatoria.");
        }
        if (medicamento.getRegistroInvima() != null && medicamento.getRegistroInvima().isBlank()) {
            throw new BadRequestException("Si se informa el registro INVIMA, no puede quedar vacío.");
        }

        if (medicamento.getLotes() != null) {
            int totalCantidad = 0;
            for (LoteMedicamento lote : medicamento.getLotes()) {
                if (lote == null) {
                    throw new BadRequestException("Existen lotes nulos dentro del producto.");
                }
                if (lote.getNumeroLote() == null || lote.getNumeroLote().isBlank()) {
                    throw new BadRequestException("El número de lote es obligatorio.");
                }
                if (lote.getCantidad() == null || lote.getCantidad() <= 0) {
                    throw new BadRequestException("La cantidad por lote debe ser mayor a cero.");
                }
                if (lote.getPrecioCompraLote() == null || lote.getPrecioCompraLote() < 0) {
                    throw new BadRequestException("El precio de compra del lote es obligatorio y no puede ser negativo.");
                }
                if (lote.getFechaIngresoLote() == null) {
                    throw new BadRequestException("La fecha de ingreso del lote es obligatoria.");
                }
                if (lote.getFechaVencimientoLote() == null) {
                    throw new BadRequestException("La fecha de vencimiento del lote es obligatoria.");
                }
                totalCantidad += lote.getCantidad();
            }
            if (medicamento.getCantidadActual() != null && totalCantidad > 0 && medicamento.getCantidadActual() != totalCantidad) {
                throw new BadRequestException("La cantidad actual debe coincidir con la suma de los lotes del producto.");
            }
            if (totalCantidad > 0) {
                medicamento.setCantidadActual(totalCantidad);
            }
        }

        if (esNuevo && (medicamento.getRegistroInvima() == null || medicamento.getRegistroInvima().isBlank())) {
            throw new BadRequestException("El registro INVIMA es obligatorio para medicamentos.");
        }
    }
}
