package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FacturaDocumento;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.repository.FacturaRepository;
import com.uniminuto.clinica.repository.FacturaRepository.CitaFacturada;
import com.uniminuto.clinica.repository.FacturaRepository.FacturaSaldo;
import com.uniminuto.clinica.repository.FacturaRepository.Producto;
import com.uniminuto.clinica.repository.FacturaRepository.Servicio;
import com.uniminuto.clinica.models.DescuentoRq;
import com.uniminuto.clinica.service.FacturacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Emite la prefactura al finalizar la atención y genera la factura definitiva con los productos
 * entregados. Controla el saldo, las devoluciones y la anulación.
 */
@Service
@RequiredArgsConstructor
public class FacturacionServiceImpl implements FacturacionService {

    private static final Set<String> METODOS = Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "NEQUI", "DAVIPLATA");
    private final FacturaRepository facturas;
    private final TarifaRepository tarifas;

    /** Valida las tarifas y guarda sus precios e IVA para conservar los valores de la atención. */
    @Transactional
    @Override
    public long prefacturar(long citaId, List<Long> tarifaIds, long usuarioId) {
        if (tarifaIds == null || tarifaIds.isEmpty()
                || tarifaIds.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(tarifaIds).size() != tarifaIds.size()) {
            throw new BadRequestException("Selecciona tarifas válidas y sin repetir.");
        }
        var cita = bloquearCitaAtendida(citaId);
        validarDocumentoUnico(citaId, "PREFACTURA");

        var serviciosSeleccionados = new HashSet<IdentidadServicio>();
        var servicios = tarifaIds.stream().map(tarifaId -> {
            var tarifa = tarifas.buscarVigenteParaMedico(cita.medicoId(), tarifaId)
                    .orElseThrow(() -> new BadRequestException("Selecciona tarifas vigentes aplicables al médico."));
            String nombre = Normalizer.normalize(tarifa.nombreServicio(), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "").strip().replaceAll("(?U)\\s+", " ").toLowerCase(Locale.ROOT);
            if (!serviciosSeleccionados.add(new IdentidadServicio(tarifa.especializacionId(), nombre))) {
                throw new BadRequestException("El servicio «" + tarifa.nombreServicio()
                        + "» aparece más de una vez para la misma especialidad.");
            }
            return new Servicio(tarifa.id(), tarifa.nombreServicio(), tarifa.valorBase(), tarifa.ivaPorcentaje());
        }).toList();

        var medicamentos = facturas.medicamentosPrescritos(citaId);
        /**
         * Calcula los importes de cada documento y deja el motivo cuando una factura no requiere
         * pago.
         */
        return guardar("PREFACTURA", cita, servicios, medicamentos, null, usuarioId);
    }

    /** Identifica un servicio por su especialidad y su nombre. */
    private record IdentidadServicio(Long especializacionId, String nombre) {
    }

    /**
     * Cobra los servicios de la prefactura y los productos entregados cuando ya no quedan unidades
     * pendientes.
     */
    @Transactional
    @Override
    public long facturar(long citaId, long usuarioId, DescuentoRq descuento) {
        var cita = bloquearCitaAtendida(citaId);
        validarDocumentoUnico(citaId, "FACTURA");

        facturas.bloquearDespachos(citaId);
        var pendientes = facturas.pendientes(citaId);
        if (!pendientes.isEmpty()) {
            throw new BadRequestException("La cita todavía tiene medicamentos pendientes de dispensación.");
        }

        var servicios = facturas.serviciosPrefacturados(citaId);
        if (servicios.isEmpty()) {
            throw new BadRequestException("Se requiere una prefactura válida con al menos un servicio.");
        }

        var entregas = facturas.entregas(citaId);
        return guardar("FACTURA", cita, servicios, entregas, descuento, usuarioId);
    }

    /** Devuelve el motivo que impide emitir la factura cuando la cita aún no cumple las condiciones. */
    @Override
    @Transactional(readOnly = true)
    public String impedimentoEmision(long citaId) {
        if (!facturas.citaAtendida(citaId)) {
            return "La cita no existe o no se ha atendido.";
        }
        if (facturas.facturaEmitida(citaId)) {
            return "La cita ya tiene una factura. Consulta el historial.";
        }
        if (!facturas.pendientes(citaId).isEmpty()) {
            return "La cita todavía tiene medicamentos pendientes de dispensación.";
        }
        return facturas.serviciosPrefacturados(citaId).isEmpty()
                ? "Se requiere una prefactura válida con al menos un servicio." : null;
    }

    /** Valida el saldo para evitar sobrepagos y actualiza el estado con el abono registrado. */
    @Transactional
    @Override
    public String pagar(long facturaId, BigDecimal monto, String metodo, String observacion, long usuarioId) {
        if (monto == null || monto.signum() <= 0 || monto.scale() > 2 || metodo == null || !METODOS.contains(metodo)) {
            throw new BadRequestException("Indica un monto positivo y un método de pago válido.");
        }
        var factura = bloquearFactura(facturaId);
        if (!Set.of("PENDIENTE", "PAGO_PARCIAL").contains(factura.estado())) {
            throw new BadRequestException("La factura no admite pagos en su estado actual.");
        }
        if (facturas.tieneDevoluciones(facturaId)) {
            throw new BadRequestException("La factura está en proceso de devolución.");
        }
        var saldo = factura.total().subtract(pagos(facturaId));
        if (monto.compareTo(saldo) > 0) {
            throw new BadRequestException("El pago supera el saldo de la factura.");
        }

        facturas.insertarPago(facturaId, monto, metodo, observacion, usuarioId);
        String estado = monto.compareTo(saldo) == 0 ? "PAGADA" : "PAGO_PARCIAL";
        facturas.actualizarEstado(facturaId, estado);
        return estado;
    }

    /** Conserva el documento y registra el motivo, el usuario y la fecha de anulación. */
    @Transactional
    @Override
    public void anular(long facturaId, String motivo, long usuarioId) {
        if (motivo == null || motivo.isBlank()) {
            throw new BadRequestException("El motivo de anulación es obligatorio.");
        }
        var factura = bloquearFactura(facturaId);
        if ("ANULADA".equals(factura.estado()) || pagos(facturaId).signum() != 0) {
            throw new BadRequestException("La factura ya está anulada o todavía tiene pagos por devolver.");
        }
        facturas.anular(facturaId, motivo, usuarioId);
    }

    /** Conserva el pago original y registra la referencia de su devolución. */
    @Transactional
    @Override
    public void devolver(long facturaId, long pagoId, String referencia, long usuarioId) {
        if (referencia == null || referencia.isBlank() || referencia.trim().length() > 150) {
            throw new BadRequestException("Indica la referencia del comprobante de devolución.");
        }
        var factura = bloquearFactura(facturaId);
        if ("ANULADA".equals(factura.estado())) {
            throw new BadRequestException("La factura está anulada.");
        }
        if (!facturas.pagoExiste(pagoId, facturaId)) {
            throw new BadRequestException("El pago no pertenece a la factura.");
        }
        if (facturas.pagoDevuelto(pagoId)) {
            throw new BadRequestException("Ese pago ya fue devuelto.");
        }
        facturas.insertarDevolucion(pagoId, referencia.trim(), usuarioId);
        String estado = pagos(facturaId).signum() == 0 ? "PENDIENTE" : "PAGO_PARCIAL";
        facturas.actualizarEstado(facturaId, estado);
    }

    /** Consulta las facturas por propietario o mascota. */
    @Override
    public List<Map<String, Object>> historial(Long clienteId, Long mascotaId) {
        if (clienteId == null && mascotaId == null) {
            throw new BadRequestException("Filtra por propietario o mascota.");
        }
        return facturas.historial(clienteId, mascotaId);
    }

    /** Consulta los pagos. */
    @Override
    public List<Map<String, Object>> listarPagos(long facturaId) {
        return facturas.listarPagos(facturaId);
    }

    /** Prepara el detalle financiero que se incluirá en el PDF. */
    @Transactional(readOnly = true)
    @Override
    public FacturaDocumento documento(long facturaId, Long citaId) {
        var cabecera = facturas.cabecera(facturaId, citaId);
        BigDecimal pagado = pagos(facturaId);
        BigDecimal saldo = null;
        if ("FACTURA".equals(cabecera.get("tipo_documento"))) {
            saldo = "ANULADA".equals(cabecera.get("estado")) ? BigDecimal.ZERO
                    : ((BigDecimal) cabecera.get("total")).subtract(pagado).max(BigDecimal.ZERO);
        }
        return new FacturaDocumento(facturas.datosClinica(), cabecera, facturas.lineas(facturaId), pagado,
                saldo, LocalDateTime.now(ZoneId.of("America/Bogota")));
    }

    /** Persiste la cabecera, los detalles y los impuestos del documento. */
    private long guardar(String tipo, CitaFacturada cita, List<Servicio> servicios, List<Producto> productos,
                         DescuentoRq descuento, long usuarioId) {
        var subtotalServicios = servicios.stream().map(Servicio::precio).reduce(BigDecimal.ZERO, BigDecimal::add);
        var ivaServicios = servicios.stream().map(servicio -> iva(servicio.precio(), servicio.ivaPorcentaje()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var subtotal = productos.stream().map(Producto::subtotal).reduce(subtotalServicios, BigDecimal::add);
        var iva = productos.stream().map(producto -> iva(producto.subtotal(), producto.ivaPorcentaje()))
                .reduce(ivaServicios, BigDecimal::add);
        var totalAntesDescuento = subtotal.add(iva);
        var importeDescuento = validarDescuento(descuento, totalAntesDescuento);
        String prefijo = "FACTURA".equals(tipo) ? "FC-" : "PF-";
        String numero = prefijo + UUID.randomUUID().toString().replace("-", "").substring(0, 17);
        var total = totalAntesDescuento.subtract(importeDescuento);
        boolean sinPago = "FACTURA".equals(tipo) && total.signum() == 0;
        var valores = new java.util.HashMap<String, Object>(Map.of("numero_factura", numero, "cita_id", cita.id(), "cliente_id", cita.clienteId(),
                "medico_id", cita.medicoId(), "subtotal", subtotal, "iva", iva,
                "total", total, "descuento", importeDescuento,
                "tipo_documento", tipo, "usuario_creacion", usuarioId));
        valores.put("especializacion_emision_id", cita.especializacionId());
        if (sinPago) {
            valores.put("estado", "NO_REQUIERE_PAGO");
            valores.put("motivo_sin_pago", "Los servicios y productos registrados no generaron cargos.");
        }
        long id = facturas.insertarFactura(valores);
        for (var servicio : servicios) {
            guardarDetalle(Map.of("factura_id", id, "tipo", "SERVICIO", "descripcion", servicio.nombre(),
                    "cantidad", 1, "precio_unitario", servicio.precio(), "subtotal", servicio.precio(),
                    "tarifa_id", servicio.id()), servicio.ivaPorcentaje(), servicio.precio());
        }
        for (var producto : productos) {
            var detalle = new java.util.HashMap<String, Object>();
            detalle.put("factura_id", id);
            detalle.put("tipo", "MEDICAMENTO");
            detalle.put("descripcion", producto.nombre());
            detalle.put("cantidad", producto.cantidad());
            detalle.put("precio_unitario", producto.precio());
            detalle.put("subtotal", producto.subtotal());
            detalle.put("medicamento_id", producto.id());
            detalle.put("despacho_detalle_id", producto.despachoDetalleId());
            guardarDetalle(detalle, producto.ivaPorcentaje(), producto.subtotal());
        }
        if (importeDescuento.signum() > 0) {
            facturas.insertarDescuento(id, usuarioId, descuento.motivo().trim());
        }
        return id;
    }

    /** Guarda el detalle. */
    private void guardarDetalle(Map<String, ?> datos, BigDecimal porcentaje, BigDecimal subtotal) {
        long id = facturas.insertarDetalle(datos);
        facturas.insertarIva(id, porcentaje, iva(subtotal, porcentaje));
    }

    /** Calcula el IVA a partir del subtotal y el porcentaje aplicable. */
    private BigDecimal iva(BigDecimal subtotal, BigDecimal porcentaje) {
        return subtotal.multiply(porcentaje).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }

    /** Comprueba que el descuento solicitado se pueda aplicar al total. */
    private BigDecimal validarDescuento(DescuentoRq descuento, BigDecimal total) {
        if (descuento == null) {
            return BigDecimal.ZERO;
        }
        var usuario = SecurityContextHolder.getContext().getAuthentication();
        if (usuario == null || usuario.getAuthorities().stream().noneMatch(autoridad ->
                Set.of("ROLE_ADMIN", "ROLE_ADMINISTRADOR").contains(autoridad.getAuthority()))) {
            throw new AccessDeniedException("Solo administración puede autorizar descuentos.");
        }
        if (descuento.monto() == null || descuento.monto().signum() <= 0 || descuento.monto().scale() > 2
                || descuento.monto().compareTo(total) >= 0 || descuento.motivo() == null
                || descuento.motivo().isBlank() || descuento.motivo().trim().length() > 200) {
            throw new BadRequestException("Indica un descuento menor que el total y su motivo.");
        }
        return descuento.monto();
    }

    /** Bloquea la cita y comprueba que ya se haya atendido. */
    private CitaFacturada bloquearCitaAtendida(long id) {
        return facturas.bloquearCitaAtendida(id);
    }

    /** Comprueba que la cita no tenga ya un documento del tipo que se intenta emitir. */
    private void validarDocumentoUnico(long citaId, String tipo) {
        if (facturas.existeDocumento(citaId, tipo)) {
            throw new BadRequestException("La cita ya tiene un documento de ese tipo.");
        }
    }

    /** Obtiene la factura con bloqueo mientras se actualiza su saldo. */
    private FacturaSaldo bloquearFactura(long id) {
        return facturas.bloquearFactura(id);
    }

    /** Calcula el importe neto de los pagos de la factura. */
    private BigDecimal pagos(long id) {
        return facturas.pagos(id);
    }


}
