package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.ConfirmarDespachoRq;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.DespachoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

@Service
public class DespachoServiceImpl implements DespachoService {
    private final DespachoRepository despachos;
    private final DespachoDetalleRepository detallesDespacho;
    private final FormulaDetalleRepository formulas;
    private final InventarioLoteRepository lotes;
    private final InventarioMovimientoRepository movimientos;
    private final DespachoHistorialRepository historial;
    private final DespachoReversionRepository reversiones;
    private final FacturaDetalleRepository lineasFacturadas;
    private final AtencionMedicaRepository atenciones;
    private final CitaRepository citas;

    public DespachoServiceImpl(DespachoRepository despachos, DespachoDetalleRepository detallesDespacho,
                               FormulaDetalleRepository formulas, InventarioLoteRepository lotes,
                               InventarioMovimientoRepository movimientos, DespachoHistorialRepository historial,
                               DespachoReversionRepository reversiones, FacturaDetalleRepository lineasFacturadas,
                               AtencionMedicaRepository atenciones,CitaRepository citas) {
        this.despachos = despachos; this.detallesDespacho = detallesDespacho; this.formulas = formulas;
        this.lotes = lotes; this.movimientos = movimientos; this.historial = historial; this.reversiones = reversiones; this.lineasFacturadas=lineasFacturadas;this.atenciones=atenciones;this.citas=citas;
    }

    @Override @Transactional
    public Despacho confirmar(ConfirmarDespachoRq request, Long usuarioId) {
        if (request == null || request.detalles() == null || request.detalles().isEmpty())
            throw new BadRequestException("Debe indicar medicamentos formulados para despachar.");
        String key = request.idempotencyKey() == null ? "" : request.idempotencyKey().trim();
        if (key.isBlank() || key.length() > 80) throw new BadRequestException("idempotencyKey es obligatorio y debe tener máximo 80 caracteres.");
        for (ConfirmarDespachoRq.Detalle d : request.detalles()) {
            if (d == null || d.formulaDetalleId() == null || d.cantidad() == null || d.cantidad().signum() <= 0)
                throw new BadRequestException("Cada detalle debe indicar fórmula y cantidad positiva.");
        }
        String requestHash = hashRequest(request);
        var existente = despachos.findByIdempotencyKey(key);
        if (existente.isPresent()) {
            if (!requestHash.equals(existente.get().getRequestHash()))
                throw new BadRequestException("La clave idempotente ya fue utilizada con un contenido diferente.");
            return existente.get();
        }

        Despacho despacho = new Despacho();
        despacho.setNumero("DSP-" + UUID.randomUUID()); despacho.setIdempotencyKey(key); despacho.setRequestHash(requestHash);
        despacho.setEstado("EN_PREPARACION"); despacho.setUsuarioId(usuarioId); despacho.setCreadoEn(LocalDateTime.now());
        despacho = despachos.saveAndFlush(despacho);
        boolean faltante = false;
        boolean huboEntrega = false;
        for (ConfirmarDespachoRq.Detalle linea : request.detalles()) {
            if (linea == null || linea.formulaDetalleId() == null || linea.cantidad() == null || linea.cantidad().signum() <= 0)
                throw new BadRequestException("Cada detalle debe indicar fórmula y cantidad positiva.");
            FormulaDetalle formula = formulas.findLockedById(linea.formulaDetalleId())
                    .orElseThrow(() -> new BadRequestException("El detalle de fórmula no existe."));
            BigDecimal pendiente = formula.getCantidadPrescrita().subtract(formula.getCantidadDispensada());
            if (linea.cantidad().compareTo(pendiente) > 0)
                throw new BadRequestException("La cantidad solicitada supera la cantidad prescrita pendiente.");
            BigDecimal restante = linea.cantidad();
            var lotesFefo = lotes.findDisponiblesFefoBloqueados(formula.getMedicamento().getId(), LocalDate.now());
            for (InventarioLote lote : lotesFefo) {
                if (restante.signum() == 0) break;
                BigDecimal entregado = lote.getCantidadDisponible().min(restante);
                if (entregado.signum() > 0) huboEntrega = true;
                lote.setCantidadDisponible(lote.getCantidadDisponible().subtract(entregado));
                restante = restante.subtract(entregado);
                lotes.save(lote);
                DespachoDetalle detalle = new DespachoDetalle();
                detalle.setDespacho(despacho); detalle.setFormulaDetalle(formula); detalle.setLote(lote);
                detalle.setCantidad(entregado); detalle.setCreadoEn(LocalDateTime.now()); detallesDespacho.save(detalle);
                InventarioMovimiento movimiento = new InventarioMovimiento();
                movimiento.setLote(lote); movimiento.setTipo("SALIDA"); movimiento.setCantidad(entregado);
                movimiento.setReferenciaTipo("DESPACHO"); movimiento.setReferenciaId(despacho.getId());
                movimiento.setMotivo("Dispensación FEFO de fórmula " + formula.getId());
                movimiento.setUsuarioId(usuarioId); movimiento.setCreadoEn(LocalDateTime.now()); movimientos.save(movimiento);
            }
            BigDecimal entregadoLinea = linea.cantidad().subtract(restante);
            formula.setCantidadDispensada(formula.getCantidadDispensada().add(entregadoLinea));
            formulas.save(formula);
            if (restante.signum() > 0) faltante = true;
        }
        despacho.setEstado(faltante ? (huboEntrega ? "PARCIAL" : "PENDIENTE_DISPONIBILIDAD") : "DESPACHADO");
        despacho.setConfirmadoEn(LocalDateTime.now());
        despacho = despachos.save(despacho);
        DespachoHistorial evento = new DespachoHistorial();
        evento.setDespachoId(despacho.getId()); evento.setEstadoAnterior("EN_PREPARACION");
        evento.setEstadoNuevo(despacho.getEstado()); evento.setMotivo("Confirmación de dispensación");
        evento.setUsuarioId(usuarioId); evento.setCreadoEn(LocalDateTime.now()); historial.save(evento);
        return despacho;
    }

    private static String hashRequest(ConfirmarDespachoRq request) {
        String canonical = request.detalles().stream()
                .map(d -> d.formulaDetalleId() + ":" + d.cantidad().stripTrailingZeros().toPlainString())
                .sorted(Comparator.naturalOrder()).reduce((a, b) -> a + "|" + b).orElse("");
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 no está disponible", e); }
    }

    @Override public java.util.List<FormulaDetalle> pendientes() { return formulas.findPendientesDeDispensar(); }
    @Override @Transactional(readOnly=true)
    public java.util.List<java.util.Map<String,Object>> buscarPendientes(Long formulaId,Integer mascotaId,String propietario,LocalDateTime desde,LocalDateTime hasta){
        validarRango(desde,hasta);String q=propietario==null||propietario.isBlank()?null:propietario.trim();
        java.util.List<java.util.Map<String,Object>> out=new java.util.ArrayList<>();
        for(FormulaDetalle f:formulas.buscarPendientes(formulaId,mascotaId,q,desde,hasta)){
            AtencionMedica a=atenciones.findById(f.getAtencionId()).orElse(null);if(a==null)continue;
            Cita cita=citas.findById(a.getCitaId()).orElse(null);if(cita==null)continue;
            var existencias=lotes.findByMedicamentoIdOrderByFechaVencimientoAsc(f.getMedicamento().getId()).stream()
                    .filter(l->!l.getFechaVencimiento().isBefore(LocalDate.now())&&l.getCantidadDisponible().signum()>0)
                    .map(l->mapa("loteId",l.getId(),"numeroLote",l.getNumeroLote(),"vence",l.getFechaVencimiento(),"disponible",l.getCantidadDisponible(),"precioVenta",l.getPrecioVentaUnitario())).toList();
            out.add(mapa("formulaId",f.getId(),"numeroFormula","FOR-"+f.getId(),"atencionId",a.getId(),"mascota",cita.getMascota().getNombreMascota(),"mascotaId",cita.getMascota().getMascotaId(),
                    "propietario",cita.getCliente().getNombres()+" "+cita.getCliente().getApellidos(),"documentoPropietario",cita.getCliente().getNumeroDocumento(),"cita",cita.getFechaHora(),
                    "veterinario",cita.getMedico().getNombres()+" "+cita.getMedico().getApellidos(),"medicamento",f.getMedicamento().getNombre(),"cantidadPrescrita",f.getCantidadPrescrita(),
                    "cantidadDispensada",f.getCantidadDispensada(),"pendiente",f.getCantidadPrescrita().subtract(f.getCantidadDispensada()),"dosis",f.getDosis(),"frecuencia",f.getFrecuencia(),
                    "duracion",f.getDuracion(),"via",f.getViaAdministracion(),"indicaciones",f.getIndicaciones(),"lotesDisponibles",existencias));
        }return out;
    }
    @Override @Transactional(readOnly=true)
    public java.util.List<java.util.Map<String,Object>> historial(LocalDateTime desde,LocalDateTime hasta,Integer mascotaId,String propietario){
        validarRango(desde,hasta);String q=propietario==null||propietario.isBlank()?null:propietario.toLowerCase(java.util.Locale.ROOT).trim();
        java.util.List<java.util.Map<String,Object>> out=new java.util.ArrayList<>();
        for(Despacho d:despachos.findAllByOrderByCreadoEnDesc()){
            if(desde!=null&&d.getCreadoEn().isBefore(desde)||hasta!=null&&d.getCreadoEn().isAfter(hasta))continue;
            var ds=detallesDespacho.findByDespachoIdOrderByIdAsc(d.getId());
            boolean matches=mascotaId==null&&q==null;
            for(DespachoDetalle det:ds){AtencionMedica a=atenciones.findById(det.getFormulaDetalle().getAtencionId()).orElse(null);if(a==null)continue;Cita c=citas.findById(a.getCitaId()).orElse(null);if(c==null)continue;
                boolean pet=mascotaId==null||mascotaId.equals(c.getMascota().getMascotaId());String person=(c.getCliente().getNombres()+" "+c.getCliente().getApellidos()+" "+c.getCliente().getNumeroDocumento()).toLowerCase(java.util.Locale.ROOT);
                boolean owner=q==null||person.contains(q);if(pet&&owner)matches=true;}
            if(matches)out.add(mapa("despacho",d,"detalles",ds,"historial",historial.findByDespachoIdOrderByCreadoEnAsc(d.getId())));
        }return out;
    }
    private static void validarRango(LocalDateTime desde,LocalDateTime hasta){if(desde!=null&&hasta!=null&&desde.isAfter(hasta))throw new BadRequestException("La fecha inicial no puede ser posterior a la final.");}
    private static java.util.Map<String,Object> mapa(Object... pares){java.util.Map<String,Object> m=new java.util.LinkedHashMap<>();for(int i=0;i<pares.length;i+=2)m.put(String.valueOf(pares[i]),pares[i+1]);return m;}
    @Override public java.util.List<DespachoDetalle> detalles(Long despachoId) {
        if (despachoId == null || !despachos.existsById(despachoId)) throw new BadRequestException("El despacho no existe.");
        return detallesDespacho.findByDespachoIdOrderByIdAsc(despachoId);
    }

    @Override @Transactional
    public Despacho revertir(Long despachoId, String motivo, Long usuarioId) {
        if (motivo == null || motivo.isBlank() || motivo.length() > 500)
            throw new BadRequestException("La reversión requiere un motivo de máximo 500 caracteres.");
        Despacho despacho = despachos.findLockedById(despachoId)
                .orElseThrow(() -> new BadRequestException("El despacho no existe."));
        if (reversiones.existsByDespachoId(despachoId)) throw new BadRequestException("Este despacho ya fue revertido.");
        if (!"DESPACHADO".equals(despacho.getEstado()) && !"PARCIAL".equals(despacho.getEstado()))
            throw new BadRequestException("Solo se pueden revertir despachos confirmados o parciales.");
        var detalles = detallesDespacho.findByDespachoIdOrderByIdAsc(despachoId);
        if (detalles.isEmpty()) throw new BadRequestException("El despacho no contiene cantidades entregadas.");
        if (detalles.stream().anyMatch(d -> lineasFacturadas.existeEnFacturaActiva(d.getId())))
            throw new BadRequestException("No se puede revertir: una o más entregas ya están incluidas en una factura. Anule primero la factura si aún no tiene pagos.");
        LocalDateTime ahora = LocalDateTime.now();
        for (DespachoDetalle detalle : detalles) {
            InventarioLote lote = lotes.findLockedById(detalle.getLote().getId())
                    .orElseThrow(() -> new BadRequestException("No se pudo bloquear el lote del despacho."));
            lote.setCantidadDisponible(lote.getCantidadDisponible().add(detalle.getCantidad()));
            lotes.save(lote);
            FormulaDetalle formula = formulas.findLockedById(detalle.getFormulaDetalle().getId())
                    .orElseThrow(() -> new BadRequestException("No se pudo bloquear la fórmula del despacho."));
            formula.setCantidadDispensada(formula.getCantidadDispensada().subtract(detalle.getCantidad()).max(BigDecimal.ZERO));
            formulas.save(formula);
            InventarioMovimiento movimiento = new InventarioMovimiento();
            movimiento.setLote(lote); movimiento.setTipo("REVERSO"); movimiento.setCantidad(detalle.getCantidad());
            movimiento.setReferenciaTipo("DESPACHO_REVERSO"); movimiento.setReferenciaId(despachoId);
            movimiento.setMotivo("Reversión autorizada: " + motivo.trim()); movimiento.setUsuarioId(usuarioId);
            movimiento.setCreadoEn(ahora); movimientos.save(movimiento);
        }
        DespachoReversion evento = new DespachoReversion(); evento.setDespachoId(despachoId);
        evento.setMotivo(motivo.trim()); evento.setAutorizadoPor(usuarioId); evento.setCreadaEn(ahora); reversiones.save(evento);
        String anterior = despacho.getEstado(); despacho.setEstado("CANCELADO"); despachos.save(despacho);
        DespachoHistorial cambio = new DespachoHistorial(); cambio.setDespachoId(despachoId);
        cambio.setEstadoAnterior(anterior); cambio.setEstadoNuevo("CANCELADO"); cambio.setMotivo("Reversión: " + motivo.trim());
        cambio.setUsuarioId(usuarioId); cambio.setCreadoEn(ahora); historial.save(cambio);
        return despacho;
    }
}
