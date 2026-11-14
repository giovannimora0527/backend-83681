package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.InventarioLoteActualizacionRq;
import com.uniminuto.clinica.models.InventarioLoteRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.InventarioLoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Implementación de los casos de uso para lotes de inventario.
 */
@Service
public class InventarioLoteServiceImpl implements InventarioLoteService {

    private final InventarioLoteRepository inventarioLoteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final UsuarioRepository usuarioRepository;

    /** Construye el servicio con los repositorios de lotes, productos, usuarios y movimientos. */
    public InventarioLoteServiceImpl(
            InventarioLoteRepository inventarioLoteRepository,
            MedicamentoRepository medicamentoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            UsuarioRepository usuarioRepository) {
        this.inventarioLoteRepository = inventarioLoteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<InventarioLote> listarLotes() {
        return inventarioLoteRepository.findAllByOrderByFechaVencimientoAsc();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<InventarioLote> listarLotesPorMedicamento(Long medicamentoId) {
        validarId(medicamentoId, "medicamento");
        validarMedicamento(medicamentoId);
        return inventarioLoteRepository.findByMedicamento_IdOrderByFechaVencimientoAsc(medicamentoId);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public InventarioLote consultarLote(Long id) {
        return obtenerLote(id);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarLote(InventarioLoteRq inventarioLoteRq, String usuarioResponsableId) {
        validarInventarioLoteRq(inventarioLoteRq);

        Long medicamentoId = inventarioLoteRq.getMedicamentoId();
        Medicamento medicamento = medicamentoRepository.findLockedById(medicamentoId)
                .orElseThrow(() -> new BadRequestException("El medicamento asociado al lote no existe."));
        Usuario usuario = validarOperadorInventario(usuarioResponsableId);
        String numeroLote = normalizarNumeroLote(inventarioLoteRq.getNumeroLote());
        validarNumeroLoteUnico(medicamentoId, numeroLote, null);

        InventarioLote inventarioLote = new InventarioLote();
        inventarioLote.setMedicamento(medicamento);
        inventarioLote.setNumeroLote(numeroLote);
        inventarioLote.setCantidad(inventarioLoteRq.getCantidad());
        inventarioLote.setPrecioCompraLote(inventarioLoteRq.getPrecioCompraLote());
        inventarioLote.setFechaVencimiento(inventarioLoteRq.getFechaVencimiento());
        inventarioLote.setFechaIngreso(LocalDateTime.now());
        inventarioLote.setActivo(inventarioLoteRq.getActivo() == null || inventarioLoteRq.getActivo());

        inventarioLote = inventarioLoteRepository.save(inventarioLote);
        medicamento.setCantidadActual(Math.addExact(cantidadActual(medicamento), inventarioLoteRq.getCantidad()));
        medicamentoRepository.save(medicamento);
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(medicamento);
        movimiento.setLote(inventarioLote);
        movimiento.setTipo("ENTRADA");
        movimiento.setCantidad(inventarioLoteRq.getCantidad());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setUsuario(usuario);
        movimiento.setObservacion("Entrada por lote " + numeroLote);
        movimientoInventarioRepository.save(movimiento);
        return respuesta("Lote guardado correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS actualizarLote(Long id, InventarioLoteActualizacionRq inventarioLoteRq) {
        validarId(id, "lote");
        validarActualizacionRq(inventarioLoteRq);
        InventarioLote inventarioLote = obtenerLote(id);

        Long medicamentoId = inventarioLoteRq.getMedicamentoId();
        if (inventarioLote.getCantidad() != null && inventarioLote.getCantidad() > 0
                && !inventarioLote.getMedicamento().getId().equals(medicamentoId)) {
            throw new BadRequestException("No se puede cambiar el producto de un lote con existencias.");
        }
        validarMedicamento(medicamentoId);
        String numeroLote = normalizarNumeroLote(inventarioLoteRq.getNumeroLote());
        validarNumeroLoteUnico(medicamentoId, numeroLote, id);

        inventarioLote.setMedicamento(obtenerMedicamento(medicamentoId));
        inventarioLote.setNumeroLote(numeroLote);
        inventarioLote.setPrecioCompraLote(inventarioLoteRq.getPrecioCompraLote());
        inventarioLote.setFechaVencimiento(inventarioLoteRq.getFechaVencimiento());
        if (inventarioLoteRq.getActivo() != null) {
            inventarioLote.setActivo(inventarioLoteRq.getActivo());
        }

        inventarioLoteRepository.save(inventarioLote);
        return respuesta("Lote actualizado correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS desactivarLote(Long id) {
        InventarioLote inventarioLote = obtenerLote(id);
        if (inventarioLote.getCantidad() != null && inventarioLote.getCantidad() > 0) {
            throw new BadRequestException("No se puede desactivar un lote que todavía tiene existencias.");
        }
        inventarioLote.setActivo(false);
        inventarioLoteRepository.save(inventarioLote);
        return respuesta("Lote desactivado correctamente.");
    }

    /** Busca un lote existente por su identificador. */
    private InventarioLote obtenerLote(Long id) {
        validarId(id, "lote");
        return inventarioLoteRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("El lote no existe."));
    }

    /** Valida los campos requeridos para registrar una entrada de lote. */
    private void validarInventarioLoteRq(InventarioLoteRq inventarioLoteRq) {
        if (inventarioLoteRq == null) {
            throw new BadRequestException("La información del lote es obligatoria.");
        }
        validarId(inventarioLoteRq.getMedicamentoId(), "medicamento");
        validarNumeroLote(inventarioLoteRq.getNumeroLote());
        if (inventarioLoteRq.getCantidad() == null || inventarioLoteRq.getCantidad() <= 0) {
            throw new BadRequestException("La cantidad de entrada debe ser mayor a cero.");
        }
        validarPrecioCompra(inventarioLoteRq.getPrecioCompraLote());
        if (inventarioLoteRq.getFechaVencimiento() == null) {
            throw new BadRequestException("La fecha de vencimiento del lote es obligatoria en la base de datos.");
        }
    }

    /** Valida los campos editables de un lote existente. */
    private void validarActualizacionRq(InventarioLoteActualizacionRq inventarioLoteRq) {
        if (inventarioLoteRq == null) {
            throw new BadRequestException("La información del lote es obligatoria.");
        }
        validarId(inventarioLoteRq.getMedicamentoId(), "medicamento");
        validarNumeroLote(inventarioLoteRq.getNumeroLote());
        validarPrecioCompra(inventarioLoteRq.getPrecioCompraLote());
        if (inventarioLoteRq.getFechaVencimiento() == null) {
            throw new BadRequestException("La fecha de vencimiento del lote es obligatoria en la base de datos.");
        }
    }

    /** Valida el formato y longitud del número de lote. */
    private void validarNumeroLote(String numeroLote) {
        if (numeroLote == null || numeroLote.isBlank() || numeroLote.trim().length() > 50) {
            throw new BadRequestException("El número de lote es obligatorio y admite máximo 50 caracteres.");
        }
    }

    /** Valida el costo de adquisición del lote. */
    private void validarPrecioCompra(BigDecimal precioCompraLote) {
        if (precioCompraLote == null || precioCompraLote.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El precio de compra del lote es obligatorio y no puede ser negativo.");
        }
    }

    /** Determina si el producto requiere registrar vencimiento por ser medicamento. */
    private boolean esMedicamento(Long medicamentoId) {
        return medicamentoRepository.findById(medicamentoId)
                .map(medicamento -> "MEDICAMENTO".equalsIgnoreCase(medicamento.getTipoProducto()))
                .orElseThrow(() -> new BadRequestException("El medicamento asociado al lote no existe."));
    }

    /** Resuelve al usuario activo autorizado para registrar una entrada. */
    private Usuario validarOperadorInventario(String usuarioPrincipal) {
        Long usuarioId;
        try {
            usuarioId = Long.valueOf(usuarioPrincipal);
        } catch (RuntimeException exception) {
            throw new BadRequestException("La identidad autenticada no contiene un id de usuario válido.");
        }
        Usuario usuario = usuarioRepository.findOneById(usuarioId)
                .orElseThrow(() -> new BadRequestException("El usuario responsable no existe."));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getPerfil() == null
                || !Boolean.TRUE.equals(usuario.getPerfil().getActivo())
                || "DESPACHO_MEDICO".equalsIgnoreCase(usuario.getPerfil().getNombre().replace(' ', '_'))) {
            throw new BadRequestException("El usuario no tiene permisos para registrar entradas de inventario.");
        }
        return usuario;
    }

    /** Devuelve el saldo actual no nulo del medicamento. */
    private int cantidadActual(Medicamento medicamento) {
        return medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
    }

    /** Confirma que el medicamento asociado exista. */
    private void validarMedicamento(Long medicamentoId) {
        validarId(medicamentoId, "medicamento");
        if (!medicamentoRepository.existsById(medicamentoId)) {
            throw new BadRequestException("El medicamento asociado al lote no existe.");
        }
    }

    /** Obtiene una referencia de persistencia al medicamento. */
    private Medicamento obtenerMedicamento(Long medicamentoId) {
        return medicamentoRepository.getReferenceById(medicamentoId);
    }

    /** Impide duplicar el identificador del lote para un mismo producto. */
    private void validarNumeroLoteUnico(Long medicamentoId, String numeroLote, Long loteId) {
        boolean existe = loteId == null
                ? inventarioLoteRepository.existsByMedicamento_IdAndNumeroLoteIgnoreCase(
                        medicamentoId, numeroLote)
                : inventarioLoteRepository.existsByMedicamento_IdAndNumeroLoteIgnoreCaseAndIdNot(
                        medicamentoId, numeroLote, loteId);

        if (existe) {
            throw new BadRequestException("Ya existe un lote con ese número para el medicamento.");
        }
    }

    /** Valida que el identificador sea positivo. */
    private void validarId(Long id, String entidad) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id del " + entidad + " debe ser mayor a cero.");
        }
    }

    /** Normaliza el número de lote a mayúsculas. */
    private String normalizarNumeroLote(String numeroLote) {
        return numeroLote.trim().toUpperCase(Locale.ROOT);
    }

    /** Crea la respuesta estándar de una operación de inventario. */
    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
