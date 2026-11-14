package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.FormulaDetalle;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.InventarioLoteRepository;
import com.uniminuto.clinica.repository.FormulaDetalleRepository;
import com.uniminuto.clinica.repository.DespachoDetalleRepository;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;

/** Implementa la creación y actualización de fórmulas médicas sin afectar existencias. */
@Service
public class FormulaServiceImpl implements FormulaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private InventarioLoteRepository inventarioLoteRepository;

    @Autowired
    private FormulaDetalleRepository formulaDetalleRepository;

    @Autowired
    private DespachoDetalleRepository despachoDetalleRepository;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<FormulaMedica> findAllFormulas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc().stream()
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

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS guardarFormula(FormulaRq formulaRq) throws BadRequestException {
        this.validarFormula(formulaRq);
        Cita cita = this.existeCita(formulaRq);
        Medicamento medicamento = this.existeMedicamento(formulaRq);
        validarDisponibilidad(medicamento, formulaRq.getCantidadPrescrita());
        FormulaMedica formulaNueva = new FormulaMedica();
        LocalDateTime ahora = LocalDateTime.now();
        formulaNueva.setNumeroFormula(generarNumeroFormula());
        formulaNueva.setCita(cita);
        formulaNueva.setMedico(cita.getMedico());
        formulaNueva.setMedicamento(medicamento);
        formulaNueva.setDosis(formulaRq.getDosis());
        formulaNueva.setIndicaciones(formulaRq.getIndicaciones());
        formulaNueva.setDuracion(formulaRq.getDuracion());
        formulaNueva.setFrecuencia(formulaRq.getFrecuencia());
        formulaNueva.setCantidadPrescrita(formulaRq.getCantidadPrescrita());
        formulaNueva.setCantidadDispensada(0);
        formulaNueva.setEstadoDispensacion("PENDIENTE");
        formulaNueva.setActivo(true);
        formulaNueva.setEstado("VIGENTE");
        formulaNueva.setFechaCreacionRegistro(ahora);
        formulaNueva.setFechaEmision(ahora);
        formulaNueva = formulaMedicaRepository.save(formulaNueva);
        sincronizarDetalleFormula(formulaNueva);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Fórmula médica guardada correctamente.");
        return rta;
    }

    /** Resuelve la cita asociada a la solicitud. */
    private Cita existeCita(FormulaRq formulaRq) throws BadRequestException {
        Optional<Cita> citaOptional = citaRepository.findById(formulaRq.getCitaId());
        if (citaOptional.isEmpty()) {
            throw new BadRequestException("La cita con el id proporcionado no existe.");
        }
        return citaOptional.get();
    }

    /** Resuelve el medicamento asociado a la solicitud. */
    private Medicamento existeMedicamento(FormulaRq formulaRq) throws BadRequestException {
        Optional<Medicamento> medicamentoOptional = medicamentoRepository.findById(formulaRq.getMedicamentoId());
        if (medicamentoOptional.isEmpty()) {
            throw new BadRequestException("El medicamento con el id proporcionado no existe.");
        }
        return medicamentoOptional.get();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public MiRespuestaRS actualizarFormula(FormulaRq formulaRq) throws BadRequestException {
        this.validarFormula(formulaRq);
        Cita cita = this.existeCita(formulaRq);
        Medicamento medicamento = this.existeMedicamento(formulaRq);
        Optional<FormulaMedica> optionalFormulaMedica = this.formulaMedicaRepository.findLockedById(formulaRq.getId());
        if (optionalFormulaMedica.isEmpty()) {
            throw new BadRequestException("La fórmula médica con el id proporcionado no existe.");
        }
        FormulaMedica formulaExistente = optionalFormulaMedica.get();
        List<FormulaDetalle> detallesExistentes = formulaDetalleRepository
                .findByFormulaIdOrderByIdAsc(formulaExistente.getId());
        int yaDispensada = cantidadDispensada(detallesExistentes);
        if (formulaRq.getCantidadPrescrita() < yaDispensada) {
            throw new BadRequestException("La cantidad prescrita no puede ser menor a la cantidad ya dispensada.");
        }
        if (yaDispensada > 0 && (detallesExistentes.isEmpty()
                || !detallesExistentes.get(0).getMedicamentoId().equals(medicamento.getId()))) {
            throw new BadRequestException("No se puede cambiar el medicamento de una fórmula parcialmente dispensada.");
        }
        validarDisponibilidad(medicamento, Math.max(0, formulaRq.getCantidadPrescrita() - yaDispensada));
        formulaExistente.setCita(cita);
        formulaExistente.setMedico(cita.getMedico());
        formulaExistente.setMedicamento(medicamento);
        formulaExistente.setDosis(formulaRq.getDosis());
        formulaExistente.setIndicaciones(formulaRq.getIndicaciones());
        formulaExistente.setDuracion(formulaRq.getDuracion());
        formulaExistente.setFrecuencia(formulaRq.getFrecuencia());
        formulaExistente.setCantidadPrescrita(formulaRq.getCantidadPrescrita());
        formulaExistente.setFechaActualizacionRegistro(LocalDateTime.now());
        this.formulaMedicaRepository.save(formulaExistente);
        sincronizarDetalleFormula(formulaExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Fórmula médica actualizada correctamente.");
        return rta;
    }


    /** Valida las reglas mínimas de una fórmula médica. */
    private void validarFormula(FormulaRq formulaRq) throws BadRequestException {
        if (formulaRq == null) {
            throw new BadRequestException("La información de la fórmula es obligatoria.");
        }
        if (formulaRq.getCantidadPrescrita() == null || formulaRq.getCantidadPrescrita() <= 0) {
            throw new BadRequestException("La cantidad prescrita debe ser mayor a cero.");
        }
        if (formulaRq.getCitaId() == null || formulaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita es obligatorio y debe ser mayor a cero.");
        }
        if (formulaRq.getCitaId() == null || formulaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita es obligatorio y debe ser mayor a cero.");
        }
        if (formulaRq.getDosis() == null || formulaRq.getDosis().isEmpty()) {
            throw new BadRequestException("La dosis de la fórmula es obligatoria.");
        }
        if (formulaRq.getIndicaciones() == null || formulaRq.getIndicaciones().isEmpty()) {
            throw new BadRequestException("Las indicaciones de la fórmula son obligatorias.");
        }
        if(formulaRq.getDuracion() == null || formulaRq.getDuracion().isEmpty()) {
            throw new BadRequestException("La duración de la fórmula es obligatoria.");
        }
        if(formulaRq.getFrecuencia() == null || formulaRq.getFrecuencia().isEmpty()) {
            throw new BadRequestException("La frecuencia de la fórmula es obligatoria.");
        }

    }

    /** Comprueba el saldo de lotes vigentes sin descontarlo durante la formulación. */
    private void validarDisponibilidad(Medicamento medicamento, int cantidadRequerida) {
        long saldoEnLotes = inventarioLoteRepository.buscarLotesDisponibles(medicamento.getId(), LocalDate.now())
                .stream().mapToLong(lote -> Math.max(0, lote.getCantidad() == null ? 0 : lote.getCantidad())).sum();
        int saldoGeneral = medicamento.getCantidadActual() == null ? 0 : medicamento.getCantidadActual();
        int disponible = (int) Math.min(saldoEnLotes, saldoGeneral);
        if (disponible < cantidadRequerida) {
            throw new BadRequestException("No hay suficiente inventario para formular la cantidad solicitada.");
        }
    }

    /** Mantiene la línea de fórmula que utiliza el proceso de despacho. */
    private void sincronizarDetalleFormula(FormulaMedica formula) {
        List<FormulaDetalle> detalles = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formula.getId());
        FormulaDetalle detalle = detalles.isEmpty() ? new FormulaDetalle() : detalles.get(0);
        detalle.setFormulaId(formula.getId());
        detalle.setMedicamentoId(formula.getMedicamento().getId());
        detalle.setCantidadPrescrita(formula.getCantidadPrescrita());
        detalle.setDosis(formula.getDosis());
        detalle.setFrecuencia(formula.getFrecuencia());
        detalle.setDuracionTratamiento(formula.getDuracion());
        detalle.setViaAdministracion("NO_ESPECIFICADA");
        detalle.setIndicaciones(formula.getIndicaciones());
        formulaDetalleRepository.save(detalle);
    }

    /** Agrega al encabezado los datos de detalle y dispensación para las respuestas actuales. */
    private void cargarDetalleEnFormula(FormulaMedica formula) {
        List<FormulaDetalle> detalles = formulaDetalleRepository.findByFormulaIdOrderByIdAsc(formula.getId());
        if (detalles.isEmpty()) {
            formula.setCantidadPrescrita(0);
            formula.setCantidadDispensada(0);
            formula.setEstadoDispensacion("PENDIENTE");
            return;
        }
        FormulaDetalle detalle = detalles.get(0);
        formula.setMedicamento(medicamentoRepository.findById(detalle.getMedicamentoId()).orElse(null));
        formula.setDosis(detalle.getDosis());
        formula.setFrecuencia(detalle.getFrecuencia());
        formula.setDuracion(detalle.getDuracionTratamiento());
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
