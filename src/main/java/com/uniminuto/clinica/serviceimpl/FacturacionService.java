package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.util.PdfUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FacturacionService {
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final TarifaServicioRepository tarifaRepository;
    private final FacturaRepository facturaRepository;
    private final FacturaDetalleRepository detalleRepository;
    private final PagoFacturaRepository pagoRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecializacionRepository especializacionRepository;
    private final MedicoRepository medicoRepository;
    private final DespachoInventarioRepository despachoRepository;

    @Value("${CLINICA_FACTURACION_NOMBRE:Clínica Veterinaria Uniminuto}")
    private String clinicaNombre;
    @Value("${CLINICA_FACTURACION_NIT:DEMO-NIT-NO-VALIDO}")
    private String clinicaNit;
    @Value("${CLINICA_FACTURACION_DIRECCION:Dirección ficticia para demostración}")
    private String clinicaDireccion;
    @Value("${CLINICA_FACTURACION_TELEFONO:000 000 0000}")
    private String clinicaTelefono;
    @Value("${CLINICA_FACTURACION_CORREO:demo@clinica.invalid}")
    private String clinicaCorreo;

    public FacturacionService(TarifaServicioRepository tarifaRepository, FacturaRepository facturaRepository,
            FacturaDetalleRepository detalleRepository, PagoFacturaRepository pagoRepository,
            CitaRepository citaRepository, UsuarioRepository usuarioRepository,
            EspecializacionRepository especializacionRepository, MedicoRepository medicoRepository,
            DespachoInventarioRepository despachoRepository) {
        this.tarifaRepository = tarifaRepository;
        this.facturaRepository = facturaRepository;
        this.detalleRepository = detalleRepository;
        this.pagoRepository = pagoRepository;
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.especializacionRepository = especializacionRepository;
        this.medicoRepository = medicoRepository;
        this.despachoRepository = despachoRepository;
    }

    @Transactional(readOnly = true)
    public List<TarifaServicioRs> tarifas() {
        LocalDate hoy = LocalDate.now();
        return tarifaRepository.findByActivoTrueOrderByNombreAsc().stream()
                .filter(t -> vigente(t, hoy)).map(this::convertirTarifa).toList();
    }

    @Transactional(readOnly = true)
    public List<TarifaServicioRs> todasLasTarifas() {
        return tarifaRepository.findAllByOrderByNombreAsc().stream().map(this::convertirTarifa).toList();
    }

    @Transactional
    public TarifaServicioRs crearTarifa(TarifaServicioRq request) {
        RelacionesTarifa relaciones = validarTarifa(request);
        TarifaServicio tarifa = new TarifaServicio();
        aplicarTarifa(tarifa, request, relaciones);
        tarifa.setActivo(true);
        return convertirTarifa(tarifaRepository.save(tarifa));
    }

    @Transactional
    public TarifaServicioRs actualizarTarifa(Long id, TarifaServicioRq request) {
        if (id == null) throw new BadRequestException("El identificador de la tarifa es obligatorio.");
        RelacionesTarifa relaciones = validarTarifa(request);
        TarifaServicio tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La tarifa indicada no existe."));
        aplicarTarifa(tarifa, request, relaciones);
        return convertirTarifa(tarifaRepository.save(tarifa));
    }

    @Transactional
    public TarifaServicioRs cambiarEstadoTarifa(Long id, Boolean activo) {
        if (id == null || activo == null) throw new BadRequestException("El identificador y el estado son obligatorios.");
        TarifaServicio tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La tarifa indicada no existe."));
        tarifa.setActivo(activo);
        return convertirTarifa(tarifaRepository.save(tarifa));
    }

    @Transactional(readOnly = true)
    public FacturaRs vistaPrevia(FacturarRq request) {
        Cita cita = validarCitaParaFacturar(request, false);
        CalculoFactura calculo = calcularFactura(cita, descuentoSolicitado(request));
        FacturaRs result = calculo.factura();
        result.setVistaPrevia(true);
        result.setEstado("VISTA PREVIA");
        result.setFechaCreacion(LocalDateTime.now());
        result.setPagado(CERO);
        result.setSaldo(result.getTotal());
        return result;
    }

    @Transactional
    public FacturaRs facturar(FacturarRq request) {
        Cita cita = validarCitaParaFacturar(request, true);
        if (facturaRepository.existsByCita_Id(cita.getId()))
            throw new BadRequestException("La cita ya tiene factura; consulta o anula la factura existente.");
        CalculoFactura calculo = calcularFactura(cita, descuentoSolicitado(request));
        FacturaRs preview = calculo.factura();
        Factura factura = new Factura();
        factura.setCita(cita);
        factura.setUsuarioCreacion(usuarioActual());
        factura.setSubtotal(preview.getSubtotal());
        factura.setDescuento(preview.getDescuento());
        factura.setImpuesto(preview.getImpuesto());
        factura.setTotal(preview.getTotal());
        factura.setEstado(preview.getTotal().signum() == 0 ? "PAGADA" : "PENDIENTE");
        factura.setFechaCreacion(LocalDateTime.now());
        factura = facturaRepository.save(factura);

        for (LineaFactura linea : calculo.lineas()) {
            FacturaDetalle detalle = new FacturaDetalle();
            detalle.setFactura(factura);
            detalle.setTarifa(linea.tarifa());
            detalle.setDespacho(linea.despacho());
            detalle.setDescripcion(linea.descripcion());
            detalle.setCantidad(linea.cantidad());
            detalle.setValorUnitario(linea.valorUnitario());
            detalle.setImpuestoPorcentaje(linea.impuestoPorcentaje());
            detalle.setDescuento(linea.descuento());
            detalle.setTotalLinea(linea.getTotalLinea());
            detalleRepository.save(detalle);
        }
        return convertir(factura);
    }

    /** Adds a later inventory dispatch to the appointment's existing invoice exactly once. */
    @Transactional
    public void registrarDespachoEnFactura(Long despachoId) {
        DespachoInventario despacho = despachoRepository.findById(despachoId)
                .orElseThrow(() -> new BadRequestException("No se encontró el despacho para facturarlo."));
        Long citaId = despacho.getFormula().getCita().getId();
        Factura factura = facturaRepository.findByCita_Id(citaId).orElse(null);
        if (factura == null || "ANULADA".equalsIgnoreCase(factura.getEstado())
                || detalleRepository.existsByDespacho_Id(despachoId)) return;
        if (despacho.getCantidadDespachada() == null || despacho.getCantidadDespachada() <= 0) return;

        FormulaMedica formula = despacho.getFormula();
        Medicamento medicamento = formula.getMedicamento();
        BigDecimal unitario = monto(medicamento.getPrecioPublico());
        int cantidad = despacho.getCantidadDespachada();
        BigDecimal subtotalLinea = unitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
        String descripcion = descripcionMedicamento(medicamento, formula, despacho.getUsuario());

        FacturaDetalle detalle = new FacturaDetalle();
        detalle.setFactura(factura);
        detalle.setDespacho(despacho);
        detalle.setDescripcion(descripcion);
        detalle.setCantidad(cantidad);
        detalle.setValorUnitario(unitario);
        detalle.setImpuestoPorcentaje(CERO);
        detalle.setDescuento(CERO);
        detalle.setTotalLinea(subtotalLinea);
        detalleRepository.save(detalle);

        factura.setSubtotal(factura.getSubtotal().add(subtotalLinea).setScale(2, RoundingMode.HALF_UP));
        factura.setTotal(factura.getSubtotal().subtract(factura.getDescuento()).add(factura.getImpuesto())
                .setScale(2, RoundingMode.HALF_UP));
        actualizarEstadoPorPagos(factura);
        facturaRepository.save(factura);
    }

    @Transactional(readOnly = true)
    public FacturaRs obtener(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Factura no encontrada."));
        return convertir(factura);
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(Long id) {
        FacturaRs f = obtener(id);
        List<String> lines = new ArrayList<>();
        lines.add("Factura " + f.getId() + " | Estado: " + f.getEstado());
        lines.add("Fecha de emisión: " + f.getFechaCreacion());
        lines.add("CLÍNICA: " + f.getClinica().getNombre() + " | NIT: " + f.getClinica().getNit());
        lines.add("Dirección: " + f.getClinica().getDireccion() + " | Teléfono: " + f.getClinica().getTelefono());
        lines.add("Correo: " + f.getClinica().getCorreo());
        if (Boolean.TRUE.equals(f.getClinica().getDatosDemostracion()))
            lines.add("DATOS FICTICIOS DE DEMOSTRACIÓN - NO VÁLIDO COMO FACTURA OFICIAL");
        lines.add("PROPIETARIO: " + f.getClienteNombre() + " | Documento: " + f.getClienteDocumento()
                + " | Teléfono: " + f.getClienteTelefono());
        lines.add("MASCOTA: " + f.getMascotaNombre() + " | Especie: " + f.getMascotaEspecie() + " | Raza: " + f.getMascotaRaza());
        lines.add("Cita: " + f.getCitaId() + " | Fecha: " + f.getCitaFecha() + " | Motivo: " + f.getMotivoConsulta());
        lines.add("Profesional: " + f.getMedicoNombre() + " | Especialidad: " + f.getEspecializacion());
        lines.add("DETALLE DE COBRO:");
        for (DetalleFacturaRs line : f.getDetalles())
            lines.add(line.getDescripcion() + " | Cantidad: " + line.getCantidad() + " | Unitario: "
                    + line.getValorUnitario() + " | Descuento: " + line.getDescuento() + " | Total: " + line.getTotalLinea());
        lines.add("Subtotal: " + f.getSubtotal() + " | Descuento: " + f.getDescuento() + " | Impuestos: " + f.getImpuesto());
        lines.add("TOTAL A PAGAR: " + f.getTotal() + " | Pagado: " + f.getPagado() + " | Saldo: " + f.getSaldo());
        for (PagoFacturaRs payment : f.getPagos())
            lines.add("Pago " + payment.getFecha() + " | " + payment.getMetodo() + " | " + payment.getMonto());
        if (f.getMotivoAnulacion() != null) lines.add("Motivo de anulación: " + f.getMotivoAnulacion());
        return PdfUtil.generar("Factura de servicio #" + id, lines);
    }

    @Transactional
    public FacturaRs registrarPago(Long facturaId, PagoFacturaRq request) {
        Factura factura = facturaRepository.findByIdForUpdate(facturaId)
                .orElseThrow(() -> new BadRequestException("Factura no encontrada."));
        if ("ANULADA".equals(factura.getEstado())) throw new BadRequestException("No se reciben pagos para una factura anulada.");
        if (request == null || request.getMonto() == null || request.getMonto().signum() <= 0)
            throw new BadRequestException("El monto del pago debe ser mayor a cero.");
        if (request.getMetodo() == null || !List.of("EFECTIVO", "TARJETA", "TRANSFERENCIA", "OTRO")
                .contains(request.getMetodo().trim().toUpperCase()))
            throw new BadRequestException("Método permitido: EFECTIVO, TARJETA, TRANSFERENCIA u OTRO.");
        BigDecimal amount = monto(request.getMonto());
        BigDecimal pagado = pagoRepository.totalPagado(facturaId);
        BigDecimal saldo = factura.getTotal().subtract(pagado);
        if (amount.compareTo(saldo) > 0) throw new BadRequestException("El pago supera el saldo pendiente.");
        PagoFactura pago = new PagoFactura();
        pago.setFactura(factura); pago.setUsuario(usuarioActual()); pago.setMonto(amount);
        pago.setMetodo(request.getMetodo().trim().toUpperCase());
        pago.setReferencia(limpiar(request.getReferencia())); pago.setFecha(LocalDateTime.now()); pagoRepository.save(pago);
        factura.setEstado(amount.compareTo(saldo) == 0 ? "PAGADA" : "PARCIAL");
        facturaRepository.save(factura);
        return convertir(factura);
    }

    @Transactional
    public FacturaRs anular(Long facturaId, String motivo) {
        if (motivo == null || motivo.isBlank() || motivo.trim().length() > 500)
            throw new BadRequestException("Indica un motivo de anulación (máximo 500 caracteres).");
        Factura factura = facturaRepository.findByIdForUpdate(facturaId)
                .orElseThrow(() -> new BadRequestException("Factura no encontrada."));
        if ("ANULADA".equals(factura.getEstado())) throw new BadRequestException("La factura ya está anulada.");
        factura.setEstado("ANULADA"); factura.setFechaAnulacion(LocalDateTime.now());
        factura.setUsuarioAnulacion(usuarioActual()); factura.setMotivoAnulacion(motivo.trim());
        facturaRepository.save(factura);
        return convertir(factura);
    }

    private Cita validarCitaParaFacturar(FacturarRq request, boolean validarExistenciaFactura) {
        if (request == null || request.getCitaId() == null || request.getCitaId() <= 0)
            throw new BadRequestException("Selecciona una cita para continuar.");
        Cita cita = validarExistenciaFactura
                ? citaRepository.findByIdForUpdate(request.getCitaId())
                    .orElseThrow(() -> new BadRequestException("La cita indicada no existe."))
                : citaRepository.findById(request.getCitaId())
                    .orElseThrow(() -> new BadRequestException("La cita indicada no existe."));
        if ("CANCELADA".equalsIgnoreCase(cita.getEstado()) || "ANULADA".equalsIgnoreCase(cita.getEstado()))
            throw new BadRequestException("No se factura una cita cancelada.");
        if (!"ATENDIDA".equalsIgnoreCase(cita.getEstado()))
            throw new BadRequestException("Primero debe finalizarse la atención médica.");
        return cita;
    }

    private CalculoFactura calcularFactura(Cita cita, BigDecimal descuento) {
        Medico medico = cita.getMedico();
        LocalDate hoy = LocalDate.now();
        TarifaServicio tarifa = tarifaRepository.vigentesPorMedico(medico.getId(), hoy).stream().findFirst()
                .or(() -> tarifaRepository.vigentesPorEspecializacion(medico.getEspecializacion().getId(), hoy).stream().findFirst())
                .orElseThrow(() -> new BadRequestException("No hay tarifa vigente para la especialización de este médico."));

        List<LineaFactura> lineas = new ArrayList<>();
        String especialidad = medico.getEspecializacion().getNombre();
        String profesional = nombreMedico(medico);
        lineas.add(new LineaFactura(tarifa, null,
                "Consulta " + valorSeguro(especialidad) + " · " + profesional,
                1, monto(tarifa.getValor()), monto(tarifa.getImpuestoPorcentaje())));

        for (DespachoInventario despacho : despachoRepository.findDespachosFacturablesPorCita(cita.getId())) {
            FormulaMedica formula = despacho.getFormula();
            Medicamento medicamento = formula.getMedicamento();
            lineas.add(new LineaFactura(null, despacho,
                    descripcionMedicamento(medicamento, formula, despacho.getUsuario()),
                    despacho.getCantidadDespachada(), monto(medicamento.getPrecioPublico()), CERO));
        }

        BigDecimal subtotal = lineas.stream().map(LineaFactura::bruto).reduce(CERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        if (descuento.signum() < 0 || descuento.compareTo(subtotal) > 0)
            throw new BadRequestException("El descuento debe estar entre cero y el subtotal.");
        BigDecimal restante = descuento;
        BigDecimal impuestoTotal = CERO;
        BigDecimal total = CERO;
        for (LineaFactura linea : lineas) {
            BigDecimal descuentoLinea = restante.min(linea.bruto());
            restante = restante.subtract(descuentoLinea);
            linea.setDescuento(descuentoLinea);
            BigDecimal base = linea.bruto().subtract(descuentoLinea);
            BigDecimal impuesto = base.multiply(linea.impuestoPorcentaje())
                    .divide(CIEN, 2, RoundingMode.HALF_UP);
            linea.setTotalLinea(base.add(impuesto).setScale(2, RoundingMode.HALF_UP));
            impuestoTotal = impuestoTotal.add(impuesto);
            total = total.add(linea.getTotalLinea());
        }

        FacturaRs result = new FacturaRs();
        completarContexto(result, cita);
        result.setSubtotal(subtotal); result.setDescuento(descuento); result.setImpuesto(impuestoTotal);
        result.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        result.setDetalles(lineas.stream().map(this::convertirLinea).toList());
        result.setPagos(List.of());
        return new CalculoFactura(result, lineas);
    }

    private void completarContexto(FacturaRs result, Cita cita) {
        result.setCitaId(cita.getId());
        ClinicaFacturaRs clinica = new ClinicaFacturaRs();
        clinica.setNombre(clinicaNombre); clinica.setNit(clinicaNit); clinica.setDireccion(clinicaDireccion);
        clinica.setTelefono(clinicaTelefono); clinica.setCorreo(clinicaCorreo);
        clinica.setDatosDemostracion(clinicaNit != null && clinicaNit.toUpperCase(java.util.Locale.ROOT).startsWith("DEMO"));
        result.setClinica(clinica);
        Cliente cliente = cita.getCliente();
        result.setClienteNombre(cliente.getNombres() + " " + cliente.getApellidos());
        result.setClienteDocumento(cliente.getTipoDocumento() + " " + cliente.getNumeroDocumento());
        result.setClienteTelefono(valorSeguro(cliente.getTelefono()));
        Mascota mascota = cita.getMascota();
        result.setMascotaNombre(mascota.getNombreMascota());
        result.setMascotaEspecie(mascota.getRaza() == null ? "No registrada" : mascota.getRaza().getEspecie());
        result.setMascotaRaza(mascota.getRaza() == null ? "No registrada" : mascota.getRaza().getNombre());
        result.setMedicoId(cita.getMedico().getId()); result.setMedicoNombre(nombreMedico(cita.getMedico()));
        result.setEspecializacion(valorSeguro(cita.getMedico().getEspecializacion().getNombre()));
        result.setCitaFecha(cita.getFechaHora()); result.setMotivoConsulta(valorSeguro(cita.getMotivo()));
    }

    private void aplicarTarifa(TarifaServicio tarifa, TarifaServicioRq request, RelacionesTarifa relaciones) {
        String nombre = request.getNombre() == null || request.getNombre().isBlank()
                ? "Consulta - " + relaciones.especializacion().getNombre() : request.getNombre().trim();
        tarifa.setNombre(nombre);
        tarifa.setValor(monto(request.getValor()));
        tarifa.setImpuestoPorcentaje(request.getImpuestoPorcentaje().setScale(2, RoundingMode.HALF_UP));
        tarifa.setVigenciaDesde(request.getVigenciaDesde()); tarifa.setVigenciaHasta(request.getVigenciaHasta());
        tarifa.setEspecializacion(relaciones.especializacion()); tarifa.setMedico(relaciones.medico());
    }

    private RelacionesTarifa validarTarifa(TarifaServicioRq request) {
        if (request == null || request.getEspecializacionId() == null || request.getValor() == null
                || request.getValor().signum() < 0 || request.getImpuestoPorcentaje() == null
                || request.getImpuestoPorcentaje().signum() < 0
                || request.getImpuestoPorcentaje().compareTo(CIEN) > 0 || request.getVigenciaDesde() == null
                || (request.getVigenciaHasta() != null && request.getVigenciaHasta().isBefore(request.getVigenciaDesde()))
                || (request.getNombre() != null && request.getNombre().trim().length() > 120))
            throw new BadRequestException("Tarifa inválida: revisa especialización, nombre, valor, impuesto y vigencia.");
        Especializacion especializacion = especializacionRepository.findById(request.getEspecializacionId())
                .orElseThrow(() -> new BadRequestException("La especialización indicada no existe."));
        Medico medico = null;
        if (request.getMedicoId() != null) {
            medico = medicoRepository.findById(request.getMedicoId())
                    .orElseThrow(() -> new BadRequestException("El médico indicado no existe."));
            if (!medico.getEspecializacion().getId().equals(especializacion.getId()))
                throw new BadRequestException("El médico debe pertenecer a la especialización de la tarifa.");
        }
        return new RelacionesTarifa(especializacion, medico);
    }

    private TarifaServicioRs convertirTarifa(TarifaServicio tarifa) {
        TarifaServicioRs out = new TarifaServicioRs();
        out.setId(tarifa.getId()); out.setNombre(tarifa.getNombre()); out.setValor(tarifa.getValor());
        out.setImpuestoPorcentaje(tarifa.getImpuestoPorcentaje()); out.setVigenciaDesde(tarifa.getVigenciaDesde());
        out.setVigenciaHasta(tarifa.getVigenciaHasta()); out.setActivo(tarifa.getActivo());
        if (tarifa.getEspecializacion() != null) {
            out.setEspecializacionId(tarifa.getEspecializacion().getId());
            out.setEspecializacionNombre(tarifa.getEspecializacion().getNombre());
        }
        if (tarifa.getMedico() != null) {
            out.setMedicoId(tarifa.getMedico().getId()); out.setMedicoNombre(nombreMedico(tarifa.getMedico()));
        }
        return out;
    }

    private DetalleFacturaRs convertirLinea(LineaFactura source) {
        DetalleFacturaRs line = new DetalleFacturaRs();
        line.setDescripcion(source.descripcion()); line.setCantidad(source.cantidad());
        line.setValorUnitario(source.valorUnitario()); line.setImpuestoPorcentaje(source.impuestoPorcentaje());
        line.setDescuento(source.descuento()); line.setTotalLinea(source.getTotalLinea());
        if (source.despacho() != null) {
            line.setDespachoId(source.despacho().getId());
            line.setFormulaId(source.despacho().getFormula().getId());
            line.setMedicamentoId(source.despacho().getFormula().getMedicamento().getId());
            line.setDespachadoPor(source.despacho().getUsuario().getUsername());
        }
        return line;
    }

    private FacturaRs convertir(Factura f) {
        FacturaRs rs = new FacturaRs();
        rs.setId(f.getId()); rs.setVistaPrevia(false); rs.setCitaId(f.getCita().getId());
        completarContexto(rs, f.getCita());
        rs.setEstado(f.getEstado()); rs.setSubtotal(f.getSubtotal()); rs.setDescuento(f.getDescuento());
        rs.setImpuesto(f.getImpuesto()); rs.setTotal(f.getTotal()); rs.setFechaCreacion(f.getFechaCreacion());
        rs.setFechaAnulacion(f.getFechaAnulacion()); rs.setMotivoAnulacion(f.getMotivoAnulacion());
        List<DetalleFacturaRs> lines = detalleRepository.findByFactura_IdOrderByIdAsc(f.getId()).stream().map(d -> {
            DetalleFacturaRs line = new DetalleFacturaRs(); line.setDescripcion(d.getDescripcion());
            line.setCantidad(d.getCantidad()); line.setValorUnitario(d.getValorUnitario());
            line.setImpuestoPorcentaje(d.getImpuestoPorcentaje()); line.setDescuento(d.getDescuento());
            line.setTotalLinea(d.getTotalLinea());
            if (d.getDespacho() != null) {
                line.setDespachoId(d.getDespacho().getId()); line.setFormulaId(d.getDespacho().getFormula().getId());
                line.setMedicamentoId(d.getDespacho().getFormula().getMedicamento().getId());
                line.setDespachadoPor(d.getDespacho().getUsuario().getUsername());
            }
            return line;
        }).toList();
        rs.setDetalles(lines);
        List<PagoFactura> payments = pagoRepository.findByFactura_IdOrderByFechaAscIdAsc(f.getId());
        BigDecimal paid = payments.stream().map(PagoFactura::getMonto).reduce(CERO, BigDecimal::add);
        rs.setPagado(paid); rs.setSaldo("ANULADA".equals(f.getEstado()) ? CERO : f.getTotal().subtract(paid));
        rs.setPagos(payments.stream().map(p -> {
            PagoFacturaRs out = new PagoFacturaRs(); out.setMonto(p.getMonto()); out.setMetodo(p.getMetodo());
            out.setReferencia(p.getReferencia()); out.setFecha(p.getFecha()); out.setUsuario(p.getUsuario().getUsername());
            return out;
        }).toList());
        return rs;
    }

    private void actualizarEstadoPorPagos(Factura factura) {
        BigDecimal pagado = pagoRepository.totalPagado(factura.getId());
        if (pagado.compareTo(factura.getTotal()) >= 0) factura.setEstado("PAGADA");
        else if (pagado.signum() > 0) factura.setEstado("PARCIAL");
        else factura.setEstado("PENDIENTE");
    }

    private boolean vigente(TarifaServicio tarifa, LocalDate fecha) {
        return Boolean.TRUE.equals(tarifa.getActivo()) && !tarifa.getVigenciaDesde().isAfter(fecha)
                && (tarifa.getVigenciaHasta() == null || !tarifa.getVigenciaHasta().isBefore(fecha));
    }

    private BigDecimal descuentoSolicitado(FacturarRq request) {
        BigDecimal descuento = request.getDescuento() == null ? CERO : monto(request.getDescuento());
        if (descuento.signum() < 0) throw new BadRequestException("El descuento no puede ser negativo.");
        return descuento;
    }

    private String descripcionMedicamento(Medicamento medicamento, FormulaMedica formula, Usuario usuario) {
        String description = "Medicamento " + medicamento.getNombre() + " · fórmula #" + formula.getId()
                + " · prescrito por " + nombreMedico(formula.getCita().getMedico())
                + " · despachado por " + usuario.getUsername();
        return description.length() <= 255 ? description : description.substring(0, 255);
    }

    private String nombreMedico(Medico medico) { return medico.getNombres() + " " + medico.getApellidos(); }
    private String valorSeguro(String valor) { return valor == null || valor.isBlank() ? "No registrado" : valor; }
    private BigDecimal monto(BigDecimal value) { return value == null ? CERO : value.setScale(2, RoundingMode.HALF_UP); }
    private String limpiar(String s) { return s == null || s.isBlank() ? null : s.trim().substring(0, Math.min(120, s.trim().length())); }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BadRequestException("Se requiere una sesión autenticada.");
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new BadRequestException("Usuario autenticado no encontrado."));
    }

    private record RelacionesTarifa(Especializacion especializacion, Medico medico) { }
    private record CalculoFactura(FacturaRs factura, List<LineaFactura> lineas) { }

    private static final class LineaFactura {
        private final TarifaServicio tarifa;
        private final DespachoInventario despacho;
        private final String descripcion;
        private final int cantidad;
        private final BigDecimal valorUnitario;
        private final BigDecimal impuestoPorcentaje;
        private BigDecimal descuento = CERO;
        private BigDecimal totalLinea = CERO;

        private LineaFactura(TarifaServicio tarifa, DespachoInventario despacho, String descripcion,
                int cantidad, BigDecimal valorUnitario, BigDecimal impuestoPorcentaje) {
            this.tarifa = tarifa; this.despacho = despacho; this.descripcion = descripcion;
            this.cantidad = cantidad; this.valorUnitario = valorUnitario; this.impuestoPorcentaje = impuestoPorcentaje;
        }
        private BigDecimal bruto() { return valorUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP); }
        private TarifaServicio tarifa() { return tarifa; }
        private DespachoInventario despacho() { return despacho; }
        private String descripcion() { return descripcion; }
        private int cantidad() { return cantidad; }
        private BigDecimal valorUnitario() { return valorUnitario; }
        private BigDecimal impuestoPorcentaje() { return impuestoPorcentaje; }
        private BigDecimal descuento() { return descuento; }
        private void setDescuento(BigDecimal value) { descuento = value; }
        private BigDecimal getTotalLinea() { return totalLinea; }
        private void setTotalLinea(BigDecimal value) { totalLinea = value; }
    }
}
