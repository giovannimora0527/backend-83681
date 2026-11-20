package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.AtencionDetalleRs;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.models.HistoriaAtencionRs;
import com.uniminuto.clinica.models.PrescripcionRq;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.util.InformePdf;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AtencionService;
import com.uniminuto.clinica.service.FacturacionService;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.service.FormulaService;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/** Reúne los antecedentes del paciente y registra el cierre clínico junto con la prefactura. */
@Service
@RequiredArgsConstructor
public class AtencionServiceImpl implements AtencionService {

    private final JdbcTemplate jdbc;
    private final CitaService citas;
    private final FormulaService formulas;
    private final FormulaMedicaRepository formulaRepository;
    private final FacturacionService facturacion;
    private final CitaRepository citaRepository;
    private final HistoriaMedicaRepository historias;
    private final AnotacionHistoriaRepository anotaciones;

    /** Reúne los datos de la cita, los antecedentes, las fórmulas y la prefactura del paciente. */
    @Override
    @Transactional(readOnly = true)
    public AtencionDetalleRs consultar(long citaId) {
        var cita = citas.detalle(citaId);
        var prefacturaId = jdbc.queryForList("""
                SELECT id FROM factura WHERE cita_id = ? AND tipo_documento = 'PREFACTURA'
                """, Long.class, citaId).stream().findFirst().orElse(null);
        var registros = historias.findByMascota_MascotaIdOrderByFechaCreacionDescIdDesc(cita.mascotaId());
        if (registros.isEmpty()) {
            return new AtencionDetalleRs(cita, List.of(), null, prefacturaId);
        }
        var notasPorHistoria = new HashMap<Long, List<HistoriaAtencionRs.Nota>>();
        for (var nota : anotaciones.findByHistoria_IdInOrderByFechaAscIdAsc(
                registros.stream().map(HistoriaMedica::getId).toList())) {
            notasPorHistoria.computeIfAbsent(nota.getHistoria().getId(), id -> new ArrayList<>())
                    .add(new HistoriaAtencionRs.Nota(nota.getId(), nota.getMedico().getId(), nota.getFecha(),
                            nota.getDescripcion(), nota.getDiagnostico(), nota.getObservaciones()));
        }
        var antecedentes = new ArrayList<HistoriaAtencionRs>();
        HistoriaAtencionRs historiaActual = null;
        for (var historia : registros) {
            Long idCita = historia.getCita() == null ? null : historia.getCita().getId();
            var registro = new HistoriaAtencionRs(historia.getId(), idCita, historia.getFechaCreacion(),
                    historia.getMotivoConsulta(), historia.getSintomas(), historia.getDiagnostico(),
                    historia.getObservaciones(), historia.getRecomendaciones(), historia.getEstado(),
                    notasPorHistoria.getOrDefault(historia.getId(), List.of()));
            if (Objects.equals(idCita, citaId)) {
                historiaActual = registro;
            } else {
                antecedentes.add(registro);
            }
        }
        return new AtencionDetalleRs(cita, antecedentes, historiaActual, prefacturaId);
    }

    /** Guarda la historia, la fecha real de atención y la prefactura en la misma operación. */
    @Override
    @Transactional
    public long finalizar(long citaId, AtencionRq solicitud, long usuarioId) {
        bloquearCitaAbierta(citaId);
        if (solicitud == null || vacio(solicitud.motivo())
                || vacio(solicitud.diagnostico())) {
            throw new BadRequestException("El motivo y el diagnóstico son obligatorios.");
        }
        var tarifaIds = resolverTarifas(solicitud);

        var cita = citaRepository.getReferenceById(citaId);
        var historia = historias.findByCita(cita).orElseGet(HistoriaMedica::new);
        historia.setCita(cita);
        historia.setMascota(cita.getMascota());
        historia.setMedico(cita.getMedico());
        historia.setMotivoConsulta(solicitud.motivo());
        historia.setSintomas(solicitud.sintomas());
        historia.setDiagnostico(solicitud.diagnostico());
        historia.setObservaciones(solicitud.observaciones());
        historia.setRecomendaciones(solicitud.recomendaciones());
        historia.setEstado("FINALIZADA");
        var fechaAtencion = LocalDateTime.now(ZoneId.of("America/Bogota"));
        if (historia.getFechaCreacion() == null) {
            historia.setFechaCreacion(fechaAtencion);
        }
        historias.save(historia);
        cita.setEstado("ATENDIDA");
        cita.setFechaAtencion(fechaAtencion);
        citaRepository.saveAndFlush(cita);
        return facturacion.prefacturar(citaId, tarifaIds, usuarioId);
    }

    /** Acepta una tarifa individual o una lista. Rechaza los identificadores inválidos o repetidos. */
    private List<Long> resolverTarifas(AtencionRq solicitud) {
        var lista = solicitud.tarifaIds();
        if (lista == null || lista.isEmpty()) {
            if (solicitud.tarifaId() == null || solicitud.tarifaId() <= 0) {
                throw new BadRequestException("Selecciona al menos una tarifa.");
            }
            return List.of(solicitud.tarifaId());
        }
        if (solicitud.tarifaId() != null) {
            throw new BadRequestException("Envía tarifaId o tarifaIds.");
        }
        if (lista.stream().anyMatch(id -> id == null || id <= 0) || lista.stream().distinct().count() != lista.size()) {
            throw new BadRequestException("Selecciona tarifas válidas y sin repetir.");
        }
        return lista;
    }

    /** Solicita guardar la prescripción para la cita que el veterinario está atendiendo. */
    @Override
    public long formular(long citaId, PrescripcionRq solicitud) {
        return formulas.formular(citaId, solicitud);
    }

    /** Consulta las unidades disponibles tras verificar el acceso a la cita. */
    @Override
    public long existencias(long citaId, long medicamentoId) {
        citas.detalle(citaId);
        return formulas.existencias(medicamentoId);
    }

    /**
     * Prepara el PDF con la fecha de emisión, el veterinario y las indicaciones generales y por
     * medicamento.
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] formula(long citaId, long formulaId) throws java.io.IOException {
        var cita = citas.detalle(citaId);
        var formula = formulaRepository.findByIdAndCita_Id(formulaId, citaId)
                .orElseThrow(() -> new BadRequestException("La fórmula no existe para esta cita."));
        var medico = formula.getMedico();
        var generado = LocalDateTime.now(ZoneId.of("America/Bogota"));
        var formatoFecha = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String contexto = "Mascota: " + cita.mascota() + "  |  Cita: " + citaId
                + "\nVeterinario emisor: " + medico.getNombres() + " " + medico.getApellidos()
                + "  |  Emisión: " + formula.getFechaEmision().format(formatoFecha);
        var filas = jdbc.query("""
                SELECT m.nombre, d.cantidad_prescrita, d.dosis, d.frecuencia, d.duracion_tratamiento,
                       d.via_administracion, COALESCE(d.indicaciones, '')
                FROM formula_detalle d JOIN medicamento m ON m.id = d.medicamento_id
                WHERE d.formula_id = ? ORDER BY d.id
                """, (rs, index) -> java.util.Arrays.<Object>asList(rs.getString(1), rs.getInt(2),
                rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7)), formulaId);

        var secciones = new ArrayList<ReporteRs>();
        secciones.add(new ReporteRs("Medicamentos prescritos", generado,
                List.of("Medicamento", "Cantidad", "Dosis", "Frecuencia", "Duración", "Vía", "Indicaciones especiales"),
                filas));
        if (!vacio(formula.getIndicaciones())) {
            secciones.add(new ReporteRs("Indicaciones generales", generado, List.of("Indicaciones"),
                    List.of(List.of(formula.getIndicaciones()))));
        }
        return InformePdf.generar("Fórmula médica · " + formula.getNumeroFormula(), generado, contexto, secciones);
    }

    /** Bloquea la cita y comprueba que todavía permita registrar atención. */
    private com.uniminuto.clinica.models.CitaCalendarioRs bloquearCitaAbierta(long citaId) {
        citaRepository.bloquearPorId(citaId)
                .orElseThrow(() -> new BadRequestException("La cita no existe."));
        var cita = citas.detalle(citaId);
        if (!List.of("PROGRAMADA", "CONFIRMADA").contains(cita.estado())) {
            throw new BadRequestException("La cita debe estar programada o confirmada.");
        }
        return cita;
    }

    /** Comprueba si el texto está ausente o solo contiene espacios. */
    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
