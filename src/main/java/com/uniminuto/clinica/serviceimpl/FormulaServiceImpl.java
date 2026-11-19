package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.models.PrescripcionRq;
import com.uniminuto.clinica.models.PrescripcionRq.MedicamentoPrescrito;
import com.uniminuto.clinica.service.CitaService;
import java.time.ZoneId;
import java.util.HashMap;
import org.springframework.transaction.annotation.Isolation;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.FormulaDetalleRepository;
import com.uniminuto.clinica.repository.DespachoDetalleRepository;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

/** Implementa la creación y actualización de fórmulas médicas sin afectar existencias. */
@Service
public class FormulaServiceImpl implements FormulaService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private CitaService citas;

    @Autowired
    private FormulaDetalleRepository formulaDetalleRepository;

    @Autowired
    private DespachoDetalleRepository despachoDetalleRepository;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FormulaMedica> findAllFormulas() {
        Long medicoId = citas.medicoAutenticado();
        var formulas = medicoId == null
                ? formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc()
                : formulaMedicaRepository.findByMedico_IdOrderByFechaCreacionRegistroDesc(medicoId);
        return formulas.stream()
                .peek(this::cargarDetalleEnFormula)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FormulaMedica> buscarFormulas(String termino) {
        String busqueda = termino == null ? "" : termino.trim().toLowerCase(Locale.ROOT);
        return findAllFormulas().stream()
                .filter(formula -> busqueda.isEmpty()
                        || String.valueOf(formula.getId()).contains(busqueda)
                        || contiene(formula.getDosis(), busqueda)
                        || contiene(formula.getIndicaciones(), busqueda)
                        || contiene(formula.getFrecuencia(), busqueda)
                        || contiene(formula.getDuracion(), busqueda)
                        || (formula.getMedicamento() != null
                        && contiene(formula.getMedicamento().getNombre(), busqueda))
                        || (formula.getCita() != null && formula.getCita().getId() != null
                        && String.valueOf(formula.getCita().getId()).contains(busqueda)))
                .toList();
    }

    /** Comprueba si un texto contiene el término de búsqueda. */
    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    /** Guarda la prescripción asociada a la atención. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public long formular(long citaId, PrescripcionRq solicitud) {
        return crear(bloquearCitaAbierta(citaId), solicitud);
    }

    /** Guarda la fórmula médica. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MiRespuestaRS guardarFormula(FormulaRq solicitud) {
        validarSolicitudIndividual(solicitud);
        crear(bloquearCitaAbierta(solicitud.getCitaId()),
                new PrescripcionRq(solicitud.getIndicaciones(), List.of(lineaIndividual(solicitud, null))));
        return respuesta("Fórmula médica guardada correctamente.");
    }

    /** Actualiza la fórmula médica. */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MiRespuestaRS actualizarFormula(FormulaRq solicitud) {
        validarSolicitudIndividual(solicitud);
        if (solicitud.getId() == null) {
            throw new BadRequestException("El id de la fórmula es obligatorio.");
        }
        Cita cita = bloquearCitaAbierta(solicitud.getCitaId());
        var formula = formulaMedicaRepository.findLockedById(solicitud.getId())
                .orElseThrow(() -> new BadRequestException("La fórmula médica no existe."));
        if (!formula.getCita().getId().equals(cita.getId())
                || !formula.getMedico().getId().equals(cita.getMedico().getId())) {
            throw new BadRequestException("La fórmula debe conservar su cita y veterinario emisor.");
        }
        var detalles = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formula.getId());
        if (!Boolean.TRUE.equals(formula.getActivo()) || !"VIGENTE".equals(formula.getEstado())
                || detalles.size() > 1) {
            throw new BadRequestException("Esta fórmula no se puede editar desde el formulario de un medicamento.");
        }
        if (formulaMedicaRepository.contarEntregas(formula.getId()) > 0) {
            throw new BadRequestException("Una fórmula con entregas registradas no se puede editar.");
        }
        var detalle = detalles.isEmpty() ? new FormulaDetalle() : detalles.getFirst();
        String viaActual = Objects.equals(detalle.getMedicamentoId(), solicitud.getMedicamentoId())
                ? detalle.getViaAdministracion() : null;
        var linea = lineaIndividual(solicitud, viaActual);
        /**
         * Suma la cantidad solicitada cuando el mismo medicamento aparece más de una vez y
         * comprueba el saldo disponible.
         */
        validarMedicamentos(List.of(linea));

        formula.setIndicaciones(solicitud.getIndicaciones());
        formula.setFechaActualizacionRegistro(LocalDateTime.now(BOGOTA));
        guardarDetalle(formula.getId(), detalle, linea);
        return respuesta("Fórmula médica actualizada correctamente.");
    }

    /** Consulta las cantidades disponibles para el medicamento indicado. */
    @Override
    public long existencias(long medicamentoId) {
        return formulaMedicaRepository.existenciasDisponibles(medicamentoId, LocalDate.now(BOGOTA))
                .orElseThrow(() -> new BadRequestException("El medicamento no existe."));
    }

    /** Valida las cantidades disponibles y registra la fórmula para la cita. */
    private long crear(Cita cita, PrescripcionRq solicitud) {
        if (solicitud == null) {
            throw new BadRequestException("La información de la fórmula es obligatoria.");
        }
        validarMedicamentos(solicitud.medicamentos());
        var ahora = LocalDateTime.now(BOGOTA);
        var formula = new FormulaMedica();
        formula.setNumeroFormula(generarNumeroFormula());
        formula.setCita(cita);
        formula.setMedico(cita.getMedico());
        formula.setIndicaciones(solicitud.indicaciones());
        formula.setFechaEmision(ahora);
        formula.setFechaCreacionRegistro(ahora);
        long id = formulaMedicaRepository.save(formula).getId();
        for (var linea : solicitud.medicamentos()) {
            guardarDetalle(id, new FormulaDetalle(), linea);
        }
        return id;
    }

    /** Protege la cita y verifica que esté programada o confirmada antes de guardar la fórmula. */
    private Cita bloquearCitaAbierta(long citaId) {
        citaRepository.bloquearPorId(citaId)
                .orElseThrow(() -> new BadRequestException("La cita no existe."));
        var cita = citas.detalle(citaId);
        if (!List.of("PROGRAMADA", "CONFIRMADA").contains(cita.estado())) {
            throw new BadRequestException("La cita debe estar programada o confirmada.");
        }
        return citaRepository.getReferenceById(citaId);
    }

    /** Valida los medicamentos. */
    private void validarMedicamentos(List<MedicamentoPrescrito> medicamentos) {
        if (medicamentos == null || medicamentos.isEmpty()) {
            throw new BadRequestException("Agrega al menos un medicamento.");
        }
        var cantidades = new HashMap<Long, Long>();
        for (var linea : medicamentos) {
            if (linea == null || linea.medicamentoId() == null || linea.medicamentoId() <= 0
                    || linea.cantidad() == null || linea.cantidad() <= 0
                    || vacio(linea.dosis()) || vacio(linea.frecuencia()) || linea.frecuencia().length() > 100
                    || vacio(linea.duracion()) || linea.duracion().length() > 50
                    || vacio(linea.via()) || linea.via().length() > 50
                    || "NO_ESPECIFICADA".equalsIgnoreCase(linea.via().trim())) {
                throw new BadRequestException("Completa el medicamento, dosis, cantidad, frecuencia, duración y vía.");
            }
            cantidades.merge(linea.medicamentoId(), linea.cantidad().longValue(), Long::sum);
        }
        cantidades.forEach((medicamento, cantidad) -> {
            if (existencias(medicamento) < cantidad) {
                throw new BadRequestException("Existencias insuficientes para el medicamento " + medicamento + ".");
            }
        });
    }

    /** Guarda el detalle. */
    private void guardarDetalle(long formulaId, FormulaDetalle detalle, MedicamentoPrescrito linea) {
        detalle.setFormulaId(formulaId);
        detalle.setMedicamentoId(linea.medicamentoId());
        detalle.setCantidadPrescrita(linea.cantidad());
        detalle.setDosis(linea.dosis());
        detalle.setFrecuencia(linea.frecuencia());
        detalle.setDuracionTratamiento(linea.duracion());
        detalle.setViaAdministracion(linea.via());
        detalle.setIndicaciones(linea.indicaciones());
        formulaDetalleRepository.save(detalle);
    }

    /** Comprueba los datos requeridos para guardar una línea de la fórmula. */
    private void validarSolicitudIndividual(FormulaRq solicitud) {
        if (solicitud == null || solicitud.getCitaId() == null || solicitud.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita es obligatorio y debe ser mayor a cero.");
        }
    }

    /** Convierte la línea recibida en el detalle de un medicamento prescrito. */
    private MedicamentoPrescrito lineaIndividual(FormulaRq solicitud, String viaActual) {
        String via = solicitud.getViaAdministracion() == null ? viaActual : solicitud.getViaAdministracion();
        return new MedicamentoPrescrito(solicitud.getMedicamentoId(), solicitud.getCantidadPrescrita(),
                solicitud.getDosis(), solicitud.getFrecuencia(), solicitud.getDuracion(), via, solicitud.getIndicaciones());
    }

    /** Comprueba si el texto está ausente o solo contiene espacios. */
    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    /** Prepara el mensaje que devuelve la operación. */
    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    /** Agrega al encabezado los datos de detalle y dispensación para las respuestas actuales. */
    private void cargarDetalleEnFormula(FormulaMedica formula) {
        List<FormulaDetalle> detalles = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formula.getId());
        formula.setDetalles(detalles);
        if (detalles.isEmpty()) {
            formula.setCantidadPrescrita(0);
            formula.setCantidadDispensada(0);
            formula.setEstadoDispensacion("PENDIENTE");
            return;
        }
        FormulaDetalle detalle = detalles.get(0);
        var medicamentos = new HashMap<Long, com.uniminuto.clinica.entity.Medicamento>();
        medicamentoRepository.findAllById(detalles.stream().map(FormulaDetalle::getMedicamentoId).distinct().toList())
                .forEach(medicamento -> medicamentos.put(medicamento.getId(), medicamento));
        for (var linea : detalles) {
            var medicamento = medicamentos.get(linea.getMedicamentoId());
            if (medicamento != null) {
                linea.setMedicamentoNombre(medicamento.getNombre());
            }
        }
        formula.setMedicamento(medicamentos.get(detalle.getMedicamentoId()));
        formula.setDosis(detalle.getDosis());
        formula.setFrecuencia(detalle.getFrecuencia());
        formula.setDuracion(detalle.getDuracionTratamiento());
        formula.setViaAdministracion(detalle.getViaAdministracion());
        formula.setCantidadPrescrita(detalles.stream().mapToInt(FormulaDetalle::getCantidadPrescrita).sum());
        int dispensada = cantidadDispensada(detalles);
        formula.setCantidadDispensada(dispensada);
        formula.setEstadoDispensacion(dispensada == 0 ? "PENDIENTE"
                : dispensada >= formula.getCantidadPrescrita() ? "DISPENSADA" : "PARCIAL");
    }

    /** Calcula unidades entregadas excluyendo despachos cancelados. */
    private int cantidadDispensada(List<FormulaDetalle> detalles) {
        long total = detalles.stream()
                .mapToLong(detalle -> java.util.Optional.ofNullable(despachoDetalleRepository
                        .sumarCantidadEntregadaPorFormulaDetalle(detalle.getId())).orElse(0L))
                .sum();
        return Math.toIntExact(total);
    }

    /** Genera el código de fórmula requerido por el esquema de base de datos. */
    private String generarNumeroFormula() {
        return "FM-" + java.util.UUID.randomUUID().toString().replace("-", "")
                .substring(0, 16).toUpperCase(Locale.ROOT);
    }
}
