package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FormulaMedicaFiltroRq;
import com.uniminuto.clinica.models.FormulaMedicaResumenRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.specification.FormulaMedicaSpecification;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.util.PaginacionUtil;
import com.uniminuto.clinica.util.ValidacionUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementación del servicio de fórmulas médicas.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /**
     * Unidades de dosis aceptadas.
     */
    public static final Set<String> UNIDADES_DOSIS = Set.of("mg", "g", "ml", "tableta", "gota", "UI");

    /**
     * Dosis máxima permitida (evita errores de digitación).
     */
    private static final BigDecimal DOSIS_MAXIMA = new BigDecimal("10000");

    /**
     * Frecuencia máxima en horas (una vez cada 3 días).
     */
    private static final int FRECUENCIA_MAXIMA_HORAS = 72;

    /**
     * Duración máxima del tratamiento en días.
     */
    private static final int DURACION_MAXIMA_DIAS = 365;

    /**
     * Longitud máxima del nombre del medicamento.
     */
    private static final int LONGITUD_MEDICAMENTO = 150;

    /**
     * Longitud máxima de las indicaciones.
     */
    private static final int LONGITUD_INDICACIONES = 2000;

    /**
     * Columnas por las que se puede ordenar el listado (columna del frontend → propiedad).
     */
    private static final Map<String, String> COLUMNAS_ORDEN = Map.of(
            "creacion", "fechaCreacion",
            "emision", "fechaEmision",
            "mascota", "mascota.nombreMascota",
            "cliente", "mascota.cliente.nombres",
            "veterinario", "medico.nombres",
            "medicamento", "medicamento",
            "estado", "activo");

    /**
     * Repositorio de fórmulas médicas.
     */
    private final FormulaMedicaRepository formulaRepository;

    /**
     * Validador de las entidades relacionadas.
     */
    private final ValidadorRelaciones validador;

    public FormulaMedicaServiceImpl(FormulaMedicaRepository formulaRepository, ValidadorRelaciones validador) {
        this.formulaRepository = formulaRepository;
        this.validador = validador;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<FormulaMedicaResumenRs> listarFormulas(FormulaMedicaFiltroRq filtro) {
        Pageable pageable = PaginacionUtil.construir(filtro, COLUMNAS_ORDEN, "creacion");
        return PaginaRs.desde(formulaRepository.findAll(FormulaMedicaSpecification.conFiltros(filtro), pageable),
                this::convertirAResumen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public FormulaMedica consultarFormula(Long formulaId) {
        return obtenerFormula(formulaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS crearFormula(FormulaMedicaRq rq) {
        // Paso 1. Validar los datos del tratamiento.
        validarDatosTratamiento(rq);

        // Paso 2. Mascota del cliente, veterinario y consulta de la misma mascota.
        Mascota mascota = validador.obtenerMascotaDeCliente(rq.getMascotaId(), rq.getClienteId());
        Medico medico = validador.obtenerMedico(rq.getMedicoId());
        HistoriaMedica historia = validador.obtenerHistoria(rq.getHistoriaId());
        if (!Objects.equals(historia.getMascota().getMascotaId(), mascota.getMascotaId())) {
            throw new BadRequestException("La consulta seleccionada pertenece a otra mascota.");
        }

        // Paso 3. Fecha de emisión y duplicados.
        validarFechaEmision(rq.getFechaEmision(), historia);
        String medicamento = rq.getMedicamento().trim();
        if (formulaRepository.existeFormulaActiva(historia.getHistoriaId(), medicamento, null)) {
            throw new ConflictException("Ya existe una fórmula activa de " + medicamento + " para esta consulta.");
        }

        // Paso 4. Guardar.
        FormulaMedica formula = new FormulaMedica();
        formula.setMascota(mascota);
        formula.setHistoriaMedica(historia);
        formula.setActivo(Boolean.TRUE);
        formula.setFechaCreacion(LocalDateTime.now());
        aplicarDatos(formula, rq, medico);
        formulaRepository.save(formula);

        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Fórmula médica creada correctamente.",
                formula.getFormulaId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Se pueden modificar el veterinario, la fecha de emisión, el medicamento,
     * la dosis, la frecuencia, la duración y las indicaciones. La mascota y la
     * consulta no se cambian porque definen a quién y en qué atención se
     * formuló; si están mal, se inactiva la fórmula y se crea una nueva.
     */
    @Override
    @Transactional
    public MiRespuestaRS editarFormula(Long formulaId, FormulaMedicaRq rq) {
        FormulaMedica formula = obtenerFormula(formulaId);
        if (Boolean.FALSE.equals(formula.getActivo())) {
            throw new ConflictException("La fórmula está inactiva y no se puede modificar.");
        }
        validarDatosTratamiento(rq);
        if (!Objects.equals(formula.getMascota().getMascotaId(), rq.getMascotaId())
                || !Objects.equals(formula.getHistoriaMedica().getHistoriaId(), rq.getHistoriaId())) {
            throw new BadRequestException("No es posible cambiar la mascota ni la consulta de una fórmula. "
                    + "Inactívela y registre una nueva.");
        }

        Medico medico = validador.obtenerMedico(rq.getMedicoId());
        validarFechaEmision(rq.getFechaEmision(), formula.getHistoriaMedica());
        if (formulaRepository.existeFormulaActiva(formula.getHistoriaMedica().getHistoriaId(),
                rq.getMedicamento().trim(), formulaId)) {
            throw new ConflictException("Ya existe otra fórmula activa de " + rq.getMedicamento().trim()
                    + " para esta consulta.");
        }

        aplicarDatos(formula, rq, medico);
        formula.setFechaModificacion(LocalDateTime.now());
        formulaRepository.save(formula);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Fórmula médica actualizada correctamente.", formulaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS inactivarFormula(Long formulaId) {
        FormulaMedica formula = obtenerFormula(formulaId);
        if (Boolean.FALSE.equals(formula.getActivo())) {
            throw new ConflictException("La fórmula ya se encuentra inactiva.");
        }
        formula.setActivo(Boolean.FALSE);
        formula.setFechaModificacion(LocalDateTime.now());
        formulaRepository.save(formula);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Fórmula médica inactivada correctamente.", formulaId);
    }

    /**
     * Obtiene una fórmula por su id.
     *
     * @param formulaId identificador de la fórmula.
     * @return fórmula encontrada.
     * @throws NotFoundException si la fórmula no existe.
     */
    private FormulaMedica obtenerFormula(Long formulaId) {
        return formulaRepository.findById(formulaId)
                .orElseThrow(() -> new NotFoundException("La fórmula médica con id " + formulaId + " no existe."));
    }

    /**
     * Valida los campos obligatorios y los valores del tratamiento
     * (dosis, unidad, frecuencia y duración).
     *
     * @param rq solicitud a validar.
     * @throws BadRequestException si algún dato falta o no es válido.
     */
    private void validarDatosTratamiento(FormulaMedicaRq rq) {
        ValidacionUtil.obligatorio(rq, "fórmula médica");
        ValidacionUtil.obligatorio(rq.getFechaEmision(), "fecha de emisión");
        ValidacionUtil.textoObligatorio(rq.getMedicamento(), "medicamento", LONGITUD_MEDICAMENTO);
        ValidacionUtil.textoObligatorio(rq.getIndicaciones(), "indicaciones", LONGITUD_INDICACIONES);

        ValidacionUtil.obligatorio(rq.getDosis(), "dosis");
        if (rq.getDosis().compareTo(BigDecimal.ZERO) <= 0 || rq.getDosis().compareTo(DOSIS_MAXIMA) > 0) {
            throw new BadRequestException("La dosis debe ser mayor que 0 y menor o igual a " + DOSIS_MAXIMA + ".");
        }
        if (rq.getDosis().stripTrailingZeros().scale() > 2) {
            throw new BadRequestException("La dosis admite máximo 2 decimales.");
        }
        if (rq.getUnidadDosis() == null || !UNIDADES_DOSIS.contains(rq.getUnidadDosis())) {
            throw new BadRequestException("La unidad de la dosis no es válida. Valores permitidos: " + UNIDADES_DOSIS + ".");
        }
        ValidacionUtil.enteroEnRango(rq.getFrecuenciaHoras(), "frecuencia (horas)", 1, FRECUENCIA_MAXIMA_HORAS);
        ValidacionUtil.enteroEnRango(rq.getDuracionDias(), "duración del tratamiento (días)", 1, DURACION_MAXIMA_DIAS);
    }

    /**
     * Valida que la fecha de emisión no sea anterior a la consulta asociada
     * ni posterior a la fecha actual.
     *
     * @param fechaEmision fecha de emisión de la fórmula.
     * @param historia     consulta asociada.
     * @throws BadRequestException si la fecha no es válida.
     */
    private void validarFechaEmision(LocalDate fechaEmision, HistoriaMedica historia) {
        if (fechaEmision.isBefore(historia.getFechaConsulta().toLocalDate())) {
            throw new BadRequestException("La fecha de emisión no puede ser anterior a la fecha de la consulta ("
                    + historia.getFechaConsulta().toLocalDate() + ").");
        }
        if (fechaEmision.isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de emisión no puede ser futura.");
        }
    }

    /**
     * Copia en la entidad los datos editables de la solicitud.
     *
     * @param formula entidad destino.
     * @param rq      solicitud con los datos.
     * @param medico  veterinario que emite la fórmula.
     */
    private void aplicarDatos(FormulaMedica formula, FormulaMedicaRq rq, Medico medico) {
        formula.setMedico(medico);
        formula.setFechaEmision(rq.getFechaEmision());
        formula.setMedicamento(rq.getMedicamento().trim());
        formula.setDosis(rq.getDosis());
        formula.setUnidadDosis(rq.getUnidadDosis());
        formula.setFrecuenciaHoras(rq.getFrecuenciaHoras());
        formula.setDuracionDias(rq.getDuracionDias());
        formula.setIndicaciones(rq.getIndicaciones().trim());
    }

    /**
     * Convierte una fórmula en su vista resumida para el listado.
     *
     * @param formula entidad completa.
     * @return vista resumida.
     */
    private FormulaMedicaResumenRs convertirAResumen(FormulaMedica formula) {
        FormulaMedicaResumenRs rs = new FormulaMedicaResumenRs();
        rs.setFormulaId(formula.getFormulaId());
        rs.setFechaEmision(formula.getFechaEmision());
        rs.setFechaCreacion(formula.getFechaCreacion());
        rs.setMascota(formula.getMascota().getNombreMascota());
        rs.setCliente(formula.getMascota().getCliente().getNombres() + " "
                + formula.getMascota().getCliente().getApellidos());
        rs.setVeterinario(formula.getMedico().getNombres() + " " + formula.getMedico().getApellidos());
        rs.setMedicamento(formula.getMedicamento());
        rs.setPosologia(formula.getDosis().stripTrailingZeros().toPlainString() + " " + formula.getUnidadDosis()
                + " cada " + formula.getFrecuenciaHoras() + " h por " + formula.getDuracionDias() + " días");
        rs.setActivo(formula.getActivo());
        return rs;
    }
}
