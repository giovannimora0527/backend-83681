package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FormulaDetalleRs;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.FormulaResumenRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.util.Estados;
import com.uniminuto.clinica.util.NombreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Lógica de negocio de las fórmulas médicas.
 *
 * Reglas: la mascota existe y pertenece a un cliente registrado; el veterinario, la historia y el
 * medicamento existen; la historia pertenece a la misma mascota; la emisión no es anterior a la consulta;
 * dosis, frecuencia y duración son valores válidos; una fórmula inactiva no se modifica y nunca se elimina.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /** Dosis máxima permitida por toma. */
    private static final BigDecimal DOSIS_MAXIMA = new BigDecimal("10000");
    /** Frecuencia máxima en horas. */
    private static final int FRECUENCIA_MAXIMA = 72;
    /** Duración máxima del tratamiento en días. */
    private static final int DURACION_MAXIMA = 365;
    /** Longitud máxima de las indicaciones. */
    private static final int MAX_INDICACIONES = 500;
    /** Formato de fecha para los mensajes al usuario. */
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Acceso a datos de fórmulas. */
    @Autowired
    private FormulaMedicaRepository formulaRepository;

    /** Acceso a datos de mascotas. */
    @Autowired
    private MascotaRepository mascotaRepository;

    /** Acceso a datos de historias clínicas. */
    @Autowired
    private HistoriaMedicaRepository historiaRepository;

    /** Acceso a datos de veterinarios. */
    @Autowired
    private MedicoRepository medicoRepository;

    /** Acceso a datos de medicamentos. */
    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /** {@inheritDoc} */
    @Override
    public List<FormulaResumenRs> listar() {
        return formulaRepository.findAllByOrderByFechaCreacionDesc().stream().map(this::aResumen).toList();
    }

    /** {@inheritDoc} */
    @Override
    public FormulaDetalleRs consultar(Long id) {
        return aDetalle(buscarFormula(id));
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS crear(FormulaMedicaRq rq) {
        validarCampos(rq);

        Mascota mascota = mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota seleccionada no existe. No se puede crear una fórmula para una mascota inexistente."));
        validarClienteDeLaMascota(mascota, rq.getClienteId());
        Medico medico = obtenerMedico(rq.getMedicoId());
        HistoriaMedica historia = historiaRepository.findById(rq.getHistoriaId())
                .orElseThrow(() -> new BadRequestException("La historia clínica seleccionada no existe."));
        if (!historia.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La historia clínica seleccionada pertenece a otra mascota.");
        }
        validarFechaEmision(rq.getFechaEmision(), historia);
        Medicamento medicamento = obtenerMedicamento(rq.getMedicamentoId());

        FormulaMedica formula = new FormulaMedica();
        formula.setMascota(mascota);
        formula.setHistoriaMedica(historia);
        formula.setFechaCreacion(LocalDateTime.now());
        formula.setEstado(Estados.ACTIVA);
        asignar(formula, rq, medico, medicamento);
        formulaRepository.save(formula);
        return respuesta(201, "Fórmula médica registrada correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS editar(Long id, FormulaMedicaRq rq) {
        FormulaMedica formula = buscarFormula(id);
        if (Estados.INACTIVA.equals(formula.getEstado())) {
            throw new ConflictException("Una fórmula inactiva no puede modificarse.");
        }
        validarCampos(rq);

        Long historiaActual = formula.getHistoriaMedica() == null ? null : formula.getHistoriaMedica().getId();
        if (!formula.getMascota().getMascotaId().equals(rq.getMascotaId())
                || !Objects.equals(historiaActual, rq.getHistoriaId())) {
            throw new ConflictException("No se puede cambiar la mascota ni la historia clínica de una fórmula ya registrada.");
        }
        validarClienteDeLaMascota(formula.getMascota(), rq.getClienteId());
        Medico medico = obtenerMedico(rq.getMedicoId());
        if (formula.getHistoriaMedica() != null) {
            validarFechaEmision(rq.getFechaEmision(), formula.getHistoriaMedica());
        }
        Medicamento medicamento = obtenerMedicamento(rq.getMedicamentoId());

        asignar(formula, rq, medico, medicamento);
        formulaRepository.save(formula);
        return respuesta(200, "Fórmula médica actualizada correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS inactivar(Long id) {
        FormulaMedica formula = buscarFormula(id);
        if (Estados.INACTIVA.equals(formula.getEstado())) {
            throw new ConflictException("La fórmula ya está inactiva.");
        }
        formula.setEstado(Estados.INACTIVA);
        formulaRepository.save(formula);
        return respuesta(200, "Fórmula inactivada correctamente. Se conserva en el historial clínico.");
    }

    /**
     * Busca una fórmula o lanza 404 si no existe.
     *
     * @param id identificador de la fórmula.
     * @return la fórmula encontrada.
     */
    private FormulaMedica buscarFormula(Long id) {
        return formulaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La fórmula médica solicitada no fue encontrada."));
    }

    /**
     * Busca el veterinario o lanza 400 si no existe.
     *
     * @param medicoId identificador del veterinario.
     * @return el veterinario encontrado.
     */
    private Medico obtenerMedico(Long medicoId) {
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El veterinario seleccionado no existe."));
    }

    /**
     * Busca el medicamento o lanza 400 si no existe.
     *
     * @param medicamentoId identificador del medicamento.
     * @return el medicamento encontrado.
     */
    private Medicamento obtenerMedicamento(Long medicamentoId) {
        return medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new BadRequestException("El medicamento seleccionado no existe."));
    }

    /**
     * La mascota debe pertenecer a un cliente registrado (y coincidir con el cliente indicado, si se envía).
     *
     * @param mascota mascota de la fórmula.
     * @param clienteId cliente indicado en la solicitud (opcional).
     */
    private void validarClienteDeLaMascota(Mascota mascota, Long clienteId) {
        Cliente cliente = mascota.getCliente();
        if (cliente == null) {
            throw new BadRequestException("La mascota no pertenece a un cliente registrado.");
        }
        if (clienteId != null && !clienteId.equals(cliente.getId())) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente indicado.");
        }
    }

    /**
     * La fecha de emisión no puede ser anterior a la fecha de la consulta asociada.
     *
     * @param emision fecha de emisión propuesta.
     * @param historia historia clínica asociada.
     */
    private void validarFechaEmision(LocalDateTime emision, HistoriaMedica historia) {
        LocalDateTime consulta = historia.getFechaConsulta();
        if (consulta != null && emision.truncatedTo(ChronoUnit.MINUTES).isBefore(consulta.truncatedTo(ChronoUnit.MINUTES))) {
            throw new BadRequestException("La fecha de emisión no puede ser anterior a la fecha de la consulta ("
                    + consulta.format(FORMATO) + ").");
        }
    }

    /**
     * Valida los datos obligatorios y que dosis, frecuencia y duración sean valores válidos.
     *
     * @param rq datos recibidos.
     */
    private void validarCampos(FormulaMedicaRq rq) {
        if (rq == null) {
            throw new BadRequestException("No se recibieron los datos de la fórmula.");
        }
        if (rq.getMascotaId() == null) {
            throw new BadRequestException("La mascota es obligatoria.");
        }
        if (rq.getHistoriaId() == null) {
            throw new BadRequestException("La historia clínica es obligatoria.");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio.");
        }
        if (rq.getFechaEmision() == null) {
            throw new BadRequestException("La fecha de emisión es obligatoria.");
        }
        if (rq.getMedicamentoId() == null) {
            throw new BadRequestException("El medicamento es obligatorio.");
        }
        if (rq.getDosis() == null || rq.getDosis().signum() <= 0 || rq.getDosis().compareTo(DOSIS_MAXIMA) > 0) {
            throw new BadRequestException("La dosis debe ser un valor mayor que 0 y no superior a " + DOSIS_MAXIMA.toPlainString() + ".");
        }
        if (rq.getUnidadDosis() == null || !Estados.UNIDADES_DOSIS.contains(rq.getUnidadDosis())) {
            throw new BadRequestException("La unidad de la dosis no es válida.");
        }
        if (rq.getFrecuenciaHoras() == null || rq.getFrecuenciaHoras() < 1 || rq.getFrecuenciaHoras() > FRECUENCIA_MAXIMA) {
            throw new BadRequestException("La frecuencia debe ser un número entero de horas entre 1 y " + FRECUENCIA_MAXIMA + ".");
        }
        if (rq.getDuracionDias() == null || rq.getDuracionDias() < 1 || rq.getDuracionDias() > DURACION_MAXIMA) {
            throw new BadRequestException("La duración del tratamiento debe ser un número entero de días entre 1 y " + DURACION_MAXIMA + ".");
        }
        if (rq.getIndicaciones() == null || rq.getIndicaciones().isBlank()) {
            throw new BadRequestException("Las indicaciones son obligatorias.");
        }
        if (rq.getIndicaciones().trim().length() > MAX_INDICACIONES) {
            throw new BadRequestException("Las indicaciones no pueden superar los " + MAX_INDICACIONES + " caracteres.");
        }
    }

    /**
     * Copia los datos recibidos a la entidad.
     *
     * @param formula entidad destino.
     * @param rq datos recibidos.
     * @param medico veterinario validado.
     * @param medicamento medicamento validado.
     */
    private void asignar(FormulaMedica formula, FormulaMedicaRq rq, Medico medico, Medicamento medicamento) {
        formula.setMedico(medico);
        formula.setMedicamento(medicamento);
        formula.setFechaEmision(rq.getFechaEmision());
        formula.setDosis(rq.getDosis());
        formula.setUnidadDosis(rq.getUnidadDosis());
        formula.setFrecuenciaHoras(rq.getFrecuenciaHoras());
        formula.setDuracionDias(rq.getDuracionDias());
        formula.setIndicaciones(rq.getIndicaciones().trim());
    }

    /**
     * Construye la respuesta estándar de una operación.
     *
     * @param status código de estado.
     * @param mensaje mensaje para el usuario.
     * @return respuesta lista.
     */
    private MiRespuestaRS respuesta(int status, String mensaje) {
        MiRespuestaRS r = new MiRespuestaRS();
        r.setStatus(status);
        r.setMessage(mensaje);
        return r;
    }

    /**
     * Convierte la entidad en el DTO resumido del listado.
     *
     * @param f fórmula.
     * @return DTO resumido.
     */
    private FormulaResumenRs aResumen(FormulaMedica f) {
        FormulaResumenRs rs = new FormulaResumenRs();
        rs.setId(f.getId());
        rs.setFechaCreacion(f.getFechaCreacion());
        rs.setFechaEmision(f.getFechaEmision());
        rs.setMascotaNombre(f.getMascota().getNombreMascota());
        rs.setClienteNombre(NombreUtil.completo(f.getMascota().getCliente()));
        rs.setMedicoNombre(NombreUtil.completo(f.getMedico()));
        rs.setMedicamentoNombre(f.getMedicamento() == null ? "" : f.getMedicamento().getNombre());
        rs.setEstado(f.getEstado());
        return rs;
    }

    /**
     * Convierte la entidad en el DTO con todos los datos.
     *
     * @param f fórmula.
     * @return DTO de detalle.
     */
    private FormulaDetalleRs aDetalle(FormulaMedica f) {
        FormulaDetalleRs rs = new FormulaDetalleRs();
        rs.setId(f.getId());
        rs.setFechaCreacion(f.getFechaCreacion());
        rs.setFechaEmision(f.getFechaEmision());
        Mascota mascota = f.getMascota();
        rs.setMascotaId(mascota.getMascotaId());
        rs.setMascotaNombre(mascota.getNombreMascota());
        if (mascota.getCliente() != null) {
            rs.setClienteId(mascota.getCliente().getId());
            rs.setClienteNombre(NombreUtil.completo(mascota.getCliente()));
        }
        if (f.getHistoriaMedica() != null) {
            rs.setHistoriaId(f.getHistoriaMedica().getId());
            rs.setFechaConsulta(f.getHistoriaMedica().getFechaConsulta());
        }
        rs.setMedicoId(f.getMedico().getId());
        rs.setMedicoNombre(NombreUtil.completo(f.getMedico()));
        if (f.getMedicamento() != null) {
            rs.setMedicamentoId(f.getMedicamento().getId());
            rs.setMedicamentoNombre(f.getMedicamento().getNombre());
        }
        rs.setDosis(f.getDosis());
        rs.setUnidadDosis(f.getUnidadDosis());
        rs.setFrecuenciaHoras(f.getFrecuenciaHoras());
        rs.setDuracionDias(f.getDuracionDias());
        rs.setIndicaciones(f.getIndicaciones());
        rs.setEstado(f.getEstado());
        return rs;
    }
}
