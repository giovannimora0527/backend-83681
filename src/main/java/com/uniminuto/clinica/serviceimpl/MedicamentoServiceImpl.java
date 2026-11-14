package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.InventarioLote;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/** Implementa administración y consultas del inventario de productos médicos. */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    private static final Pattern CODIGO_PRODUCTO = Pattern.compile("^[A-Za-z0-9]{8}$");
    private final MedicamentoRepository medicamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioLoteRepository inventarioLoteRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    /** Construye el servicio con sus repositorios de inventario y usuarios. */
    public MedicamentoServiceImpl(MedicamentoRepository medicamentoRepository,
                                  UsuarioRepository usuarioRepository,
                                  InventarioLoteRepository inventarioLoteRepository,
                                  MovimientoInventarioRepository movimientoInventarioRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inventarioLoteRepository = inventarioLoteRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarMedicamentos() {
        return medicamentoRepository.findAllByOrderByNombreAsc();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> buscarMedicamentos(String termino) {
        String busqueda = normalizarBusqueda(termino);
        return listarMedicamentos().stream()
                .filter(medicamento -> busqueda.isEmpty()
                        || contiene(medicamento.getNombre(), busqueda)
                        || contiene(medicamento.getCodigo(), busqueda)
                        || contiene(medicamento.getDescripcion(), busqueda)
                        || contiene(medicamento.getPresentacion(), busqueda))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarDisponibles() {
        return medicamentoRepository.findAllByOrderByNombreAsc().stream()
                .filter(medicamento -> cantidadDisponible(medicamento) > 0)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarAgotados() {
        return medicamentoRepository.findAllByOrderByNombreAsc().stream()
                .filter(medicamento -> cantidadDisponible(medicamento) == 0)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarStockBajo() {
        return medicamentoRepository.buscarMedicamentosConStockBajo();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<Medicamento> listarProximosAVencer(int dias) {
        if (dias < 0 || dias > 3650) {
            throw new BadRequestException("El rango de vencimiento debe estar entre 0 y 3650 días.");
        }
        Map<Long, Medicamento> medicamentos = new LinkedHashMap<>();
        inventarioLoteRepository.buscarLotesPorVencer(LocalDate.now(), LocalDate.now().plusDays(dias))
                .forEach(lote -> medicamentos.put(lote.getMedicamento().getId(), lote.getMedicamento()));
        return List.copyOf(medicamentos.values());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultarDisponibilidad(Long medicamentoId, Integer cantidadRequerida) {
        validarId(medicamentoId);
        if (cantidadRequerida == null || cantidadRequerida < 0) {
            throw new BadRequestException("La cantidad requerida no puede ser negativa.");
        }
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BadRequestException("El medicamento no existe."));
        int existencia = cantidadDisponible(medicamento);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("medicamentoId", medicamentoId);
        resultado.put("nombre", medicamento.getNombre());
        resultado.put("cantidadDisponible", existencia);
        resultado.put("cantidadRequerida", cantidadRequerida);
        resultado.put("suficiente", existencia >= cantidadRequerida);
        return resultado;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS crearMedicamento(MedicamentoRq solicitud, String usuarioResponsableId) {
        Usuario usuario = validarOperadorInventario(usuarioResponsableId);
        validarSolicitud(solicitud);
        String nombre = solicitud.getNombre().trim();
        if (medicamentoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new BadRequestException("Ya existe un medicamento con ese nombre.");
        }
        validarDatosInventario(solicitud, null);
        Medicamento medicamento = new Medicamento();
        mapearDatosEditables(medicamento, solicitud);
        medicamento.setCantidadActual(0);
        medicamento.setUsuarioCreacion(usuario);
        medicamento.setFechaCreacionRegistro(LocalDateTime.now());
        medicamento = medicamentoRepository.save(medicamento);
        int saldoInicial = solicitud.getCantidadActual();
        if (saldoInicial > 0) {
            registrarIngresoInicial(medicamento, solicitud, usuario, saldoInicial);
        }
        return respuesta("Medicamento creado correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS actualizarMedicamento(MedicamentoRq solicitud, String usuarioResponsableId) {
        validarOperadorInventario(usuarioResponsableId);
        validarSolicitud(solicitud);
        validarId(solicitud.getId());
        Medicamento medicamento = medicamentoRepository.findLockedById(solicitud.getId())
                .orElseThrow(() -> new BadRequestException("El medicamento no existe."));
        if (medicamentoRepository.existsByNombreIgnoreCaseAndIdNot(solicitud.getNombre().trim(), medicamento.getId())) {
            throw new BadRequestException("Ya existe otro medicamento con ese nombre.");
        }
        validarDatosInventario(solicitud, medicamento.getId());
        if (!solicitud.getCantidadActual().equals(medicamento.getCantidadActual())) {
            throw new BadRequestException("Las existencias solo se modifican mediante entradas, salidas o ajustes trazables.");
        }
        mapearDatosEditables(medicamento, solicitud);
        medicamento.setFechaModificacionRegistro(LocalDateTime.now());
        medicamentoRepository.save(medicamento);
        return respuesta("Medicamento actualizado correctamente.");
    }

    /** Guarda el lote inicial y el movimiento que deja trazabilidad de la entrada. */
    private void registrarIngresoInicial(Medicamento medicamento, MedicamentoRq solicitud, Usuario usuario, int cantidad) {
        InventarioLote lote = new InventarioLote();
        lote.setMedicamento(medicamento);
        lote.setNumeroLote("INICIAL-" + medicamento.getCodigo());
        lote.setCantidad(cantidad);
        lote.setPrecioCompraLote(solicitud.getPrecioCompra());
        lote.setFechaVencimiento(solicitud.getFechaVence());
        lote.setFechaIngreso(solicitud.getFechaCompra().atStartOfDay());
        lote.setActivo(true);
        lote = inventarioLoteRepository.save(lote);
        medicamento.setCantidadActual(cantidad);
        medicamentoRepository.save(medicamento);
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(medicamento);
        movimiento.setLote(lote);
        movimiento.setTipo("ENTRADA");
        movimiento.setCantidad(cantidad);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setUsuario(usuario);
        movimiento.setObservacion("Saldo inicial de inventario");
        movimientoInventarioRepository.save(movimiento);
    }

    /** Verifica los campos requeridos y la relación entre compra y vencimiento. */
    private void validarSolicitud(MedicamentoRq solicitud) {
        if (solicitud == null || solicitud.getNombre() == null || solicitud.getNombre().isBlank()) {
            throw new BadRequestException("El nombre del medicamento es obligatorio.");
        }
        if (solicitud.getFechaCompra() == null) {
            throw new BadRequestException("La fecha de compra o ingreso es obligatoria.");
        }
        String tipo = normalizarTipo(solicitud.getTipoProducto());
        if ("MEDICAMENTO".equals(tipo) && solicitud.getFechaVence() == null) {
            throw new BadRequestException("La fecha de vencimiento es obligatoria para los medicamentos.");
        }
        if (solicitud.getCantidadActual() != null && solicitud.getCantidadActual() > 0
                && solicitud.getFechaVence() == null) {
            throw new BadRequestException("La fecha de vencimiento es obligatoria para cada lote con existencias.");
        }
        if (solicitud.getFechaVence() != null && solicitud.getFechaVence().isBefore(solicitud.getFechaCompra())) {
            throw new BadRequestException("La fecha de vencimiento no puede ser anterior a la fecha de compra.");
        }
    }

    /** Valida código, tipo de producto, registro Invima, precios y existencias. */
    private void validarDatosInventario(MedicamentoRq solicitud, Long id) {
        if (solicitud.getCodigo() == null || !CODIGO_PRODUCTO.matcher(solicitud.getCodigo().trim()).matches()) {
            throw new BadRequestException("El código del producto debe tener exactamente 8 caracteres alfanuméricos.");
        }
        boolean codigoDuplicado = id == null
                ? medicamentoRepository.existsByCodigoIgnoreCase(solicitud.getCodigo().trim())
                : medicamentoRepository.existsByCodigoIgnoreCaseAndIdNot(solicitud.getCodigo().trim(), id);
        if (codigoDuplicado) {
            throw new BadRequestException("Ya existe un producto con ese código.");
        }
        String tipo = normalizarTipo(solicitud.getTipoProducto());
        if (!List.of("MEDICAMENTO", "PRODUCTO_MEDICO", "UTENSILIO").contains(tipo)) {
            throw new BadRequestException("El tipo debe ser MEDICAMENTO, PRODUCTO_MEDICO o UTENSILIO.");
        }
        if ("MEDICAMENTO".equals(tipo) && (solicitud.getRegistroInvima() == null
                || solicitud.getRegistroInvima().isBlank())) {
            throw new BadRequestException("El registro Invima es obligatorio para los medicamentos.");
        }
        if (solicitud.getRegistroInvima() != null && solicitud.getRegistroInvima().length() > 50) {
            throw new BadRequestException("El registro Invima admite máximo 50 caracteres.");
        }
        validarPrecio(solicitud.getPrecioCompra(), "compra");
        validarPrecio(solicitud.getPrecioVenta(), "venta");
        if (solicitud.getCantidadActual() == null || solicitud.getCantidadActual() < 0) {
            throw new BadRequestException("La cantidad disponible es obligatoria y no puede ser negativa.");
        }
        if (solicitud.getStockMinimo() == null || solicitud.getStockMinimo() < 0) {
            throw new BadRequestException("El stock mínimo es obligatorio y no puede ser negativo.");
        }
    }

    /** Copia los datos editables sin permitir cambios directos de existencias. */
    private void mapearDatosEditables(Medicamento medicamento, MedicamentoRq solicitud) {
        medicamento.setNombre(solicitud.getNombre().trim());
        medicamento.setDescripcion(limpiar(solicitud.getDescripcion()));
        medicamento.setPresentacion(limpiar(solicitud.getPresentacion()));
        medicamento.setFechaCompra(solicitud.getFechaCompra());
        medicamento.setFechaVence(solicitud.getFechaVence());
        medicamento.setCodigo(solicitud.getCodigo().trim().toUpperCase(Locale.ROOT));
        medicamento.setTipoProducto(tipoPersistido(solicitud.getTipoProducto()));
        medicamento.setRegistroInvima(limpiar(solicitud.getRegistroInvima()));
        medicamento.setRequiereInvima("MEDICAMENTO".equals(normalizarTipo(solicitud.getTipoProducto())));
        medicamento.setPrecioVenta(solicitud.getPrecioVenta());
        medicamento.setPrecioCompra(solicitud.getPrecioCompra());
        medicamento.setStockMinimo(solicitud.getStockMinimo());
        medicamento.setFechaProximoPedido(solicitud.getFechaProximoPedido());
    }

    /** Resuelve y valida el usuario responsable de una operación administrativa. */
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
                || !Boolean.TRUE.equals(usuario.getPerfil().getActivo())) {
            throw new BadRequestException("El usuario no está activo.");
        }
        String perfil = usuario.getPerfil().getNombre().trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if ("DESPACHO_MEDICO".equals(perfil)) {
            throw new BadRequestException("El perfil DESPACHO_MEDICO solo puede consultar el inventario.");
        }
        return usuario;
    }

    /** Valida un identificador positivo. */
    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El id del medicamento debe ser mayor a cero.");
        }
    }

    /** Calcula el saldo utilizable entre el stock general y sus lotes vigentes. */
    private int cantidadDisponible(Medicamento medicamento) {
        long lotesDisponibles = inventarioLoteRepository.buscarLotesDisponibles(
                        medicamento.getId(), LocalDate.now()).stream()
                .mapToLong(lote -> Math.max(0, lote.getCantidad() == null ? 0 : lote.getCantidad()))
                .sum();
        int saldoGeneral = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
        return (int) Math.min(Math.max(0, saldoGeneral), Math.min(Integer.MAX_VALUE, lotesDisponibles));
    }

    /** Valida que un precio exista y no sea negativo. */
    private void validarPrecio(BigDecimal precio, String tipo) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El precio de " + tipo + " es obligatorio y no puede ser negativo.");
        }
    }

    /** Normaliza el texto utilizado para búsquedas. */
    private String normalizarBusqueda(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    /** Normaliza el tipo de producto. */
    private String normalizarTipo(String tipo) {
        return tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
    }

    /** Convierte categorías de la API al enum admitido por la columna tipo. */
    private String tipoPersistido(String tipo) {
        return "PRODUCTO_MEDICO".equals(normalizarTipo(tipo)) ? "UTENSILIO" : normalizarTipo(tipo);
    }

    /** Comprueba si un campo contiene el término de búsqueda. */
    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    /** Convierte texto vacío a nulo. */
    private String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    /** Construye la respuesta estándar de operaciones del inventario. */
    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
