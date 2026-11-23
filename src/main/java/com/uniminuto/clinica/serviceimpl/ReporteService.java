package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Factura;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.models.InventarioItemRs;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.*;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FacturaRepository;
import com.uniminuto.clinica.repository.PagoFacturaRepository;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.service.InventarioService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {
    private final CitaRepository citaRepository;
    private final FacturaRepository facturaRepository;
    private final PagoFacturaRepository pagoRepository;
    private final InventarioService inventarioService;
    private final LoteMedicamentoRepository loteRepository;
    public ReporteService(CitaRepository citaRepository, FacturaRepository facturaRepository,
            PagoFacturaRepository pagoRepository, InventarioService inventarioService,
            LoteMedicamentoRepository loteRepository) {
        this.citaRepository = citaRepository; this.facturaRepository = facturaRepository;
        this.pagoRepository = pagoRepository; this.inventarioService = inventarioService;
        this.loteRepository = loteRepository;
    }

    @Transactional(readOnly = true)
    public ReporteCitasRs citas(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<Cita> source = citaRepository.findByFechaHoraBetween(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay().minusNanos(1));
        List<ReporteCitaFilaRs> rows = source.stream().sorted((a,b) -> a.getFechaHora().compareTo(b.getFechaHora())).map(c -> {
            ReporteCitaFilaRs r = new ReporteCitaFilaRs(); r.setId(c.getId()); r.setFechaHora(c.getFechaHora());
            r.setCliente(c.getCliente().getNombres() + " " + c.getCliente().getApellidos());
            r.setMascota(c.getMascota().getNombreMascota()); r.setMedico(c.getMedico().getNombres() + " " + c.getMedico().getApellidos());
            r.setEstado(c.getEstado()); return r;
        }).toList();
        ReporteCitasRs result = new ReporteCitasRs(); result.setDesde(desde); result.setHasta(hasta);
        result.setTotal(rows.size()); result.setCitas(rows); return result;
    }

    @Transactional(readOnly = true)
    public ReporteFacturacionRs facturacion(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<Factura> source = facturaRepository.findByFechaCreacionBetweenOrderByFechaCreacionAsc(
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay().minusNanos(1));
        List<ReporteFacturaFilaRs> rows = source.stream().map(f -> {
            BigDecimal paid = pagoRepository.totalPagado(f.getId());
            BigDecimal balance = "ANULADA".equals(f.getEstado()) ? BigDecimal.ZERO : f.getTotal().subtract(paid);
            ReporteFacturaFilaRs r = new ReporteFacturaFilaRs(); r.setId(f.getId()); r.setCitaId(f.getCita().getId());
            r.setFecha(f.getFechaCreacion()); r.setEstado(f.getEstado()); r.setSubtotal(f.getSubtotal());
            r.setDescuento(f.getDescuento()); r.setImpuesto(f.getImpuesto()); r.setTotal(f.getTotal());
            r.setPagado(paid); r.setSaldo(balance); return r;
        }).toList();
        ReporteFacturacionRs result = new ReporteFacturacionRs(); result.setDesde(desde); result.setHasta(hasta);
        result.setTotalFacturas(rows.size()); result.setFacturas(rows);
        result.setSubtotal(sum(rows, ReporteFacturaFilaRs::getSubtotal)); result.setDescuentos(sum(rows, ReporteFacturaFilaRs::getDescuento));
        result.setImpuestos(sum(rows, ReporteFacturaFilaRs::getImpuesto)); result.setTotal(sum(rows, ReporteFacturaFilaRs::getTotal));
        result.setPagado(sum(rows, ReporteFacturaFilaRs::getPagado)); result.setSaldo(sum(rows, ReporteFacturaFilaRs::getSaldo));
        return result;
    }

    @Transactional(readOnly = true)
    public ReporteInventarioRs inventario() {
        List<InventarioItemRs> items = inventarioService.obtenerResumen(null);
        Map<Long, BigDecimal> valorPorProducto = new java.util.HashMap<>();
        BigDecimal totalValorizado = BigDecimal.ZERO;
        for (LoteMedicamento lote : loteRepository.findAll()) {
            if (lote.getFechaVencimiento().isBefore(LocalDate.now())) {
                continue;
            }
            BigDecimal valor = lote.getCostoUnitario().multiply(BigDecimal.valueOf(lote.getCantidadDisponible()));
            totalValorizado = totalValorizado.add(valor);
            valorPorProducto.merge(lote.getMedicamento().getId(), valor, BigDecimal::add);
        }

        ReporteInventarioRs result = new ReporteInventarioRs();
        result.setFechaCorte(LocalDate.now());
        result.setProductos(items.size());
        result.setUnidadesDisponibles(items.stream().mapToInt(i -> i.getExistenciaDispensable() == null ? 0 : i.getExistenciaDispensable()).sum());
        result.setUnidadesVencidas(items.stream().mapToInt(i -> i.getExistenciaVencida() == null ? 0 : i.getExistenciaVencida()).sum());
        result.setProductosBajoStock((int) items.stream().filter(i -> "BAJO".equalsIgnoreCase(i.getEstadoStock()) || "AGOTADO".equalsIgnoreCase(i.getEstadoStock())).count());
        result.setProductosProximosAVencer((int) items.stream().filter(i -> Boolean.TRUE.equals(i.getProximoAVencer())).count());
        result.setValorInventario(totalValorizado);
        result.setProductosDetalle(items.stream().map(item -> {
            ReporteInventarioRs.Fila row = new ReporteInventarioRs.Fila();
            row.setMedicamentoId(item.getMedicamentoId()); row.setNombre(item.getNombre());
            row.setTipoProducto(item.getTipoProducto()); row.setExistenciaTotal(item.getExistenciaTotal());
            row.setExistenciaDispensable(item.getExistenciaDispensable()); row.setExistenciaVencida(item.getExistenciaVencida());
            row.setStockMinimo(item.getStockMinimo()); row.setEstadoStock(item.getEstadoStock());
            row.setVencimientoMasCercano(item.getVencimientoMasCercano());
            row.setValorExistencia(valorPorProducto.getOrDefault(item.getMedicamentoId(), BigDecimal.ZERO));
            return row;
        }).toList());
        return result;
    }

    @Transactional(readOnly = true)
    public ReporteMedicosRs medicos(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        List<Cita> citas = citaRepository.findByFechaHoraBetween(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay().minusNanos(1));
        Map<Long, ReporteMedicosRs.Fila> filas = new java.util.TreeMap<>();
        for (Cita cita : citas) {
            var medico = cita.getMedico();
            ReporteMedicosRs.Fila fila = filas.computeIfAbsent(medico.getId(), id -> {
                ReporteMedicosRs.Fila nueva = new ReporteMedicosRs.Fila();
                nueva.setMedicoId(id); nueva.setMedico(medico.getNombres() + " " + medico.getApellidos());
                nueva.setTotalCitas(0); nueva.setAtendidas(0); nueva.setCanceladas(0); nueva.setPendientes(0);
                return nueva;
            });
            fila.setTotalCitas(fila.getTotalCitas() + 1);
            String estado = cita.getEstado() == null ? "" : cita.getEstado().toUpperCase(java.util.Locale.ROOT);
            if (List.of("ATENDIDA", "COMPLETADA", "FINALIZADA").contains(estado)) fila.setAtendidas(fila.getAtendidas() + 1);
            else if (List.of("CANCELADA", "ANULADA").contains(estado)) fila.setCanceladas(fila.getCanceladas() + 1);
            else fila.setPendientes(fila.getPendientes() + 1);
        }
        List<ReporteMedicosRs.Fila> resultado = filas.values().stream().toList();
        ReporteMedicosRs report = new ReporteMedicosRs(); report.setDesde(desde); report.setHasta(hasta);
        report.setTotalCitas(citas.size()); report.setTotalAtendidas(resultado.stream().mapToInt(ReporteMedicosRs.Fila::getAtendidas).sum());
        report.setTotalCanceladas(resultado.stream().mapToInt(ReporteMedicosRs.Fila::getCanceladas).sum()); report.setMedicos(resultado);
        return report;
    }

    public byte[] inventarioExcel(ReporteInventarioRs report) {
        return excel("Inventario", List.of("ID", "Producto", "Tipo", "Existencia", "Disponible", "Vencida", "Stock mínimo", "Estado", "Próximo vencimiento", "Valor"),
                report.getProductosDetalle().stream().map(r -> List.of(String.valueOf(r.getMedicamentoId()), safe(r.getNombre()), safe(r.getTipoProducto()),
                        number(r.getExistenciaTotal()), number(r.getExistenciaDispensable()), number(r.getExistenciaVencida()), number(r.getStockMinimo()),
                        safe(r.getEstadoStock()), r.getVencimientoMasCercano() == null ? "" : r.getVencimientoMasCercano().toString(), money(r.getValorExistencia()))).toList());
    }

    public byte[] inventarioPdf(ReporteInventarioRs report) {
        List<String> lines = new ArrayList<>();
        lines.add("INVENTARIO AL " + report.getFechaCorte());
        lines.add("Productos: " + report.getProductos() + " | Unidades disponibles: " + report.getUnidadesDisponibles()
                + " | Vencidas: " + report.getUnidadesVencidas() + " | Valor: " + report.getValorInventario());
        report.getProductosDetalle().forEach(r -> lines.add(r.getNombre() + " | disponible " + r.getExistenciaDispensable()
                + " | vencida " + r.getExistenciaVencida() + " | " + r.getEstadoStock() + " | valor " + r.getValorExistencia()));
        return pdf(lines);
    }

    public byte[] medicosExcel(ReporteMedicosRs report) {
        return excel("Médicos", List.of("ID", "Médico", "Citas", "Atendidas", "Canceladas", "Pendientes"),
                report.getMedicos().stream().map(r -> List.of(String.valueOf(r.getMedicoId()), safe(r.getMedico()), number(r.getTotalCitas()),
                        number(r.getAtendidas()), number(r.getCanceladas()), number(r.getPendientes()))).toList());
    }

    public byte[] medicosPdf(ReporteMedicosRs report) {
        List<String> lines = new ArrayList<>(); lines.add("GESTION MEDICA " + report.getDesde() + " a " + report.getHasta());
        lines.add("Citas: " + report.getTotalCitas() + " | Atendidas: " + report.getTotalAtendidas() + " | Canceladas: " + report.getTotalCanceladas());
        report.getMedicos().forEach(r -> lines.add(r.getMedico() + " | citas " + r.getTotalCitas() + " | atendidas " + r.getAtendidas()
                + " | canceladas " + r.getCanceladas() + " | pendientes " + r.getPendientes()));
        return pdf(lines);
    }

    public byte[] citasExcel(ReporteCitasRs report) { return excel("Citas", List.of("ID", "Fecha y hora", "Cliente", "Mascota", "Médico", "Estado"),
            report.getCitas().stream().map(r -> List.of(String.valueOf(r.getId()), r.getFechaHora().toString(), r.getCliente(), r.getMascota(), r.getMedico(), r.getEstado())).toList()); }

    public byte[] facturacionExcel(ReporteFacturacionRs report) { return excel("Facturación", List.of("Factura", "Cita", "Fecha", "Estado", "Subtotal", "Descuento", "Impuesto", "Total", "Pagado", "Saldo"),
            report.getFacturas().stream().map(r -> List.of(String.valueOf(r.getId()), String.valueOf(r.getCitaId()), r.getFecha().toString(), r.getEstado(), r.getSubtotal().toPlainString(), r.getDescuento().toPlainString(), r.getImpuesto().toPlainString(), r.getTotal().toPlainString(), r.getPagado().toPlainString(), r.getSaldo().toPlainString())).toList()); }

    public byte[] citasPdf(ReporteCitasRs report) {
        List<String> lines = new ArrayList<>(); lines.add("REPORTE DE CITAS " + report.getDesde() + " a " + report.getHasta());
        report.getCitas().forEach(r -> lines.add(r.getFechaHora() + " | " + r.getCliente() + " | " + r.getMascota() + " | " + r.getMedico() + " | " + r.getEstado()));
        lines.add("Total de citas: " + report.getTotal()); return pdf(lines);
    }
    public byte[] facturacionPdf(ReporteFacturacionRs report) {
        List<String> lines = new ArrayList<>(); lines.add("REPORTE DE FACTURACION " + report.getDesde() + " a " + report.getHasta());
        report.getFacturas().forEach(r -> lines.add("Factura " + r.getId() + " | " + r.getFecha() + " | " + r.getEstado() + " | total " + r.getTotal() + " | pagado " + r.getPagado() + " | saldo " + r.getSaldo()));
        lines.add("Facturas: " + report.getTotalFacturas() + " | subtotal " + report.getSubtotal() + " | descuentos " + report.getDescuentos() + " | impuestos " + report.getImpuestos() + " | total " + report.getTotal());
        return pdf(lines);
    }

    private byte[] excel(String title, List<String> headers, List<List<String>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(title); Row header = sheet.createRow(0);
            CellStyle style = workbook.createCellStyle(); Font font = workbook.createFont(); font.setBold(true); style.setFont(font);
            for (int i=0;i<headers.size();i++) { Cell cell=header.createCell(i); cell.setCellValue(headers.get(i)); cell.setCellStyle(style); }
            for (int r=0;r<rows.size();r++) {
                Row row = sheet.createRow(r+1);
                for (int c=0;c<headers.size();c++) row.createCell(c).setCellValue(rows.get(r).get(c));
            }
            for (int c=0;c<headers.size();c++) sheet.autoSizeColumn(c);
            workbook.write(out); return out.toByteArray();
        } catch (IOException ex) { throw new IllegalStateException("No se pudo generar el informe Excel.", ex); }
    }

    private byte[] pdf(List<String> lines) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            for (int start=0; start<lines.size(); start+=45) {
                PDPage page = new PDPage(); document.addPage(page);
                try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                    stream.beginText(); stream.setFont(font, 9); stream.newLineAtOffset(36, 750);
                    for (String raw : lines.subList(start, Math.min(start+45, lines.size()))) {
                        String line = raw.length() > 125 ? raw.substring(0, 125) : raw;
                        stream.showText(line); stream.newLineAtOffset(0, -15);
                    }
                    stream.endText();
                }
            }
            document.save(out); return out.toByteArray();
        } catch (IOException ex) { throw new IllegalStateException("No se pudo generar el informe PDF.", ex); }
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) throw new BadRequestException("Indica un rango de fechas válido.");
        if (hasta.isAfter(LocalDate.now().plusDays(1))) throw new BadRequestException("La fecha final no puede exceder mañana.");
    }
    private String number(Integer value) { return value == null ? "0" : value.toString(); }
    private String money(BigDecimal value) { return value == null ? "0.00" : value.toPlainString(); }
    private String safe(String value) { return value == null ? "" : value; }
    private BigDecimal sum(List<ReporteFacturaFilaRs> rows, java.util.function.Function<ReporteFacturaFilaRs,BigDecimal> field) {
        return rows.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
