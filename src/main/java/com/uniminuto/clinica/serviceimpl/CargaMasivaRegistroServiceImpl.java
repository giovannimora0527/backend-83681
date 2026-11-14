package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.CargaMasivaRegistroService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

/** Persiste cada fila de importación de forma transaccional e independiente. */
@Service
public class CargaMasivaRegistroServiceImpl implements CargaMasivaRegistroService {

    private final MedicamentoRepository medicamentoRepository;
    private final InventarioLoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    /** Construye el servicio con los repositorios requeridos para el alta de un registro. */
    public CargaMasivaRegistroServiceImpl(MedicamentoRepository medicamentoRepository,
                                          InventarioLoteRepository loteRepository,
                                          MovimientoInventarioRepository movimientoRepository,
                                          UsuarioRepository usuarioRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void guardarRegistro(Map<String, String> fila, Long usuarioId) {
        Usuario usuario = usuarioRepository.findOneById(usuarioId)
                .orElseThrow(() -> new BadRequestException("El usuario responsable ya no existe."));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || usuario.getPerfil() == null
                || !Boolean.TRUE.equals(usuario.getPerfil().getActivo())) {
            throw new BadRequestException("El usuario responsable ya no está activo.");
        }
        if (medicamentoRepository.existsByNombreIgnoreCase(fila.get("nombre"))) {
            throw new BadRequestException("Ya existe un producto con el nombre " + fila.get("nombre") + ".");
        }
        if (medicamentoRepository.existsByCodigoIgnoreCase(fila.get("codigo"))) {
            throw new BadRequestException("Ya existe un producto con el código " + fila.get("codigo") + ".");
        }
        Medicamento medicamento = new Medicamento();
        medicamento.setCodigo(fila.get("codigo").toUpperCase(Locale.ROOT));
        medicamento.setNombre(fila.get("nombre"));
        medicamento.setTipoProducto("PRODUCTO_MEDICO".equals(fila.get("tipoProducto"))
                ? "UTENSILIO" : fila.get("tipoProducto"));
        medicamento.setRequiereInvima("MEDICAMENTO".equals(fila.get("tipoProducto")));
        medicamento.setRegistroInvima(fila.get("registroInvima"));
        medicamento.setDescripcion(fila.get("descripcion"));
        medicamento.setPresentacion(fila.get("presentacion"));
        medicamento.setFechaCompra(LocalDate.parse(fila.get("fechaCompra")));
        medicamento.setFechaVence(fecha(fila.get("fechaVence")));
        medicamento.setPrecioCompra(new BigDecimal(fila.get("precioCompra")));
        medicamento.setPrecioVenta(new BigDecimal(fila.get("precioVenta")));
        medicamento.setStockMinimo(Integer.parseInt(fila.get("stockMinimo")));
        medicamento.setCantidadActual(0);
        medicamento.setFechaProximoPedido(fecha(fila.get("fechaProximoPedido")));
        medicamento.setFechaCreacionRegistro(LocalDateTime.now());
        medicamento.setUsuarioCreacion(usuario);
        medicamento = medicamentoRepository.save(medicamento);

        int cantidad = Integer.parseInt(fila.get("cantidadActual"));
        if (cantidad == 0) {
            return;
        }
        InventarioLote lote = new InventarioLote();
        lote.setMedicamento(medicamento);
        lote.setNumeroLote(fila.get("numeroLote"));
        lote.setCantidad(cantidad);
        lote.setPrecioCompraLote(new BigDecimal(fila.get("precioCompraLote")));
        lote.setFechaVencimiento(fecha(fila.get("fechaVence")));
        lote.setFechaIngreso(LocalDateTime.now());
        lote.setActivo(true);
        lote = loteRepository.save(lote);
        medicamento.setCantidadActual(cantidad);
        medicamentoRepository.save(medicamento);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(medicamento);
        movimiento.setLote(lote);
        movimiento.setTipo("ENTRADA");
        movimiento.setCantidad(cantidad);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setUsuario(usuario);
        movimiento.setObservacion("Entrada de inventario por carga masiva");
        movimientoRepository.save(movimiento);
    }

    /** Convierte una fecha opcional en el tipo de dominio. */
    private LocalDate fecha(String valor) {
        return valor == null || valor.isBlank() ? null : LocalDate.parse(valor);
    }
}
