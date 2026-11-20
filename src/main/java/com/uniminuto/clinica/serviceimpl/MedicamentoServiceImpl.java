package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ProductoRq;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {
    private static final String CODIGOS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private InventarioLoteRepository inventarioLoteRepository;

    @Override public List<Medicamento> listarMedicamentos() {
        return medicamentoRepository.findAll().stream().sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre())).toList();
    }
    @Override public Medicamento consultar(Long id) {
        return medicamentoRepository.findById(id).orElseThrow(() -> new BadRequestException("El producto no existe."));
    }
    @Override @Transactional public Medicamento crear(ProductoRq rq) {
        validar(rq); Medicamento m = new Medicamento(); aplicar(m, rq);
        m.setFechaCreacionRegistro(LocalDateTime.now()); return medicamentoRepository.save(m);
    }
    @Override @Transactional public Medicamento actualizar(Long id, ProductoRq rq) {
        validar(rq); Medicamento m = consultar(id); aplicar(m, rq);
        m.setFechaModificacionRegistro(LocalDateTime.now()); return medicamentoRepository.save(m);
    }
    @Override public List<Medicamento> listarStockBajo() {
        return medicamentoRepository.findAll().stream().filter(m -> stock(m.getId()).signum() > 0
                && stock(m.getId()).compareTo(m.getStockMinimo()) <= 0).toList();
    }
    @Override public List<Medicamento> listarAgotados() {
        return medicamentoRepository.findAll().stream().filter(m -> stock(m.getId()).signum() == 0).toList();
    }
    @Override public List<Medicamento> listarPorVencer(LocalDate hasta) {
        validarFecha(hasta);
        return medicamentoRepository.findAll().stream().filter(m -> lotes(m.getId()).stream().anyMatch(l -> l.getCantidadDisponible().signum() > 0
                && !l.getFechaVencimiento().isBefore(LocalDate.now()) && !l.getFechaVencimiento().isAfter(hasta))).toList();
    }
    @Override public List<Medicamento> listarVencidos() {
        return medicamentoRepository.findAll().stream().filter(m -> lotes(m.getId()).stream()
                .anyMatch(l -> l.getCantidadDisponible().signum() > 0 && l.getFechaVencimiento().isBefore(LocalDate.now()))).toList();
    }
    private List<InventarioLote> lotes(Long id) { return inventarioLoteRepository.findByMedicamentoIdOrderByFechaVencimientoAsc(id); }
    private BigDecimal stock(Long id) { return lotes(id).stream().map(InventarioLote::getCantidadDisponible).reduce(BigDecimal.ZERO, BigDecimal::add); }
    private void validarFecha(LocalDate hasta) {
        if (hasta == null || hasta.isBefore(LocalDate.now())) throw new BadRequestException("La fecha límite debe ser hoy o futura.");
    }
    private void validar(ProductoRq rq) {
        if (rq == null || rq.nombre() == null || rq.nombre().isBlank() || rq.tipoProducto() == null)
            throw new BadRequestException("Nombre y tipo de producto son obligatorios.");
        String tipo = rq.tipoProducto().trim().toUpperCase(Locale.ROOT);
        if (!tipo.equals("MEDICAMENTO") && !tipo.equals("PRODUCTO")) throw new BadRequestException("tipoProducto debe ser MEDICAMENTO o PRODUCTO.");
        if (tipo.equals("MEDICAMENTO") && (rq.registroInvima() == null || rq.registroInvima().isBlank()))
            throw new BadRequestException("El registro INVIMA es obligatorio para medicamentos.");
        if (rq.codigo() != null && !rq.codigo().matches("[A-Za-z0-9]{8}")) throw new BadRequestException("El código debe tener exactamente 8 caracteres alfanuméricos.");
        if (rq.precioCompra() == null || rq.precioVenta() == null || rq.stockMinimo() == null
                || rq.precioCompra().signum() < 0 || rq.precioVenta().signum() < 0 || rq.stockMinimo().signum() < 0)
            throw new BadRequestException("Los precios y el stock mínimo son obligatorios y no pueden ser negativos.");
    }
    private void aplicar(Medicamento m, ProductoRq rq) {
        m.setNombre(rq.nombre().trim()); m.setDescripcion(rq.descripcion()); m.setPresentacion(rq.presentacion());
        m.setTipoProducto(rq.tipoProducto().trim().toUpperCase(Locale.ROOT)); m.setRegistroInvima(rq.registroInvima());
        m.setPrecioCompra(rq.precioCompra()); m.setPrecioVenta(rq.precioVenta()); m.setStockMinimo(rq.stockMinimo());
        m.setFechaProximoPedido(rq.fechaProximoPedido()); m.setActivo(rq.activo() == null || rq.activo());
        if (rq.codigo() == null || rq.codigo().isBlank()) {
            if (m.getCodigo() == null) { String code; do { code = generarCodigo(); } while (medicamentoRepository.existsByCodigo(code)); m.setCodigo(code); }
        } else {
            String code = rq.codigo().toUpperCase(Locale.ROOT);
            if (!code.equals(m.getCodigo()) && medicamentoRepository.existsByCodigo(code)) throw new BadRequestException("El código ya está asignado a otro producto.");
            m.setCodigo(code);
        }
    }
    private String generarCodigo() {
        StringBuilder result = new StringBuilder(8);
        for (int i = 0; i < 8; i++) result.append(CODIGOS.charAt(RANDOM.nextInt(CODIGOS.length())));
        return result.toString();
    }
}
