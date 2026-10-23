package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.EstadoHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.HistoriaMedicaFiltroRq;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.specification.HistoriaMedicaSpecification;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import com.uniminuto.clinica.util.PaginacionUtil;
import com.uniminuto.clinica.util.ValidacionUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementación del servicio de historias clínicas.
 */
@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    /**
     * Longitud máxima del motivo de consulta.
     */
    private static final int LONGITUD_MOTIVO = 255;

    /**
     * Longitud máxima de los campos de texto largo.
     */
    private static final int LONGITUD_TEXTO = 2000;

    /**
     * Columnas por las que se puede ordenar el listado (columna del frontend → propiedad).
     */
    private static final Map<String, String> COLUMNAS_ORDEN = Map.of(
            "fecha", "fechaConsulta",
            "mascota", "mascota.nombreMascota",
            "cliente", "mascota.cliente.nombres",
            "veterinario", "medico.nombres",
            "motivo", "motivoConsulta",
            "estado", "estado");

    /**
     * Repositorio de historias clínicas.
     */
    private final HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Validador de las entidades relacionadas.
     */
    private final ValidadorRelaciones validador;

    public HistoriaMedicaServiceImpl(HistoriaMedicaRepository historiaMedicaRepository,
            ValidadorRelaciones validador) {
        this.historiaMedicaRepository = historiaMedicaRepository;
        this.validador = validador;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<HistoriaMedica> listarHistorias(HistoriaMedicaFiltroRq filtro) {
        EstadoHistoria estado = convertirEstado(filtro.getEstado());
        Pageable pageable = PaginacionUtil.construir(filtro, COLUMNAS_ORDEN, "fecha");
        return PaginaRs.desde(historiaMedicaRepository.findAll(
                HistoriaMedicaSpecification.conFiltros(filtro, estado), pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public HistoriaMedica consultarHistoria(Long historiaId) {
        return validador.obtenerHistoria(historiaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS crearHistoria(HistoriaMedicaRq rq) {
        // Paso 1. Campos obligatorios.
        ValidacionUtil.obligatorio(rq, "historia clínica");
        ValidacionUtil.obligatorio(rq.getFechaConsulta(), "fecha y hora de la consulta");
        String motivo = ValidacionUtil.textoObligatorio(rq.getMotivoConsulta(), "motivo de consulta", LONGITUD_MOTIVO);
        String diagnostico = ValidacionUtil.textoObligatorio(rq.getDiagnostico(), "diagnóstico", LONGITUD_TEXTO);
        String observaciones = ValidacionUtil.textoOpcional(rq.getObservaciones(), "observaciones", LONGITUD_TEXTO);

        // Paso 2. Cliente, mascota y veterinario existen y la mascota es del cliente.
        Mascota mascota = validador.obtenerMascotaDeCliente(rq.getMascotaId(), rq.getClienteId());
        Medico medico = validador.obtenerMedico(rq.getMedicoId());

        // Paso 3. La cita existe, es de la misma mascota, está atendida y no tiene historia.
        Cita cita = validador.obtenerCita(rq.getCitaId());
        if (!Objects.equals(cita.getMascota().getMascotaId(), mascota.getMascotaId())) {
            throw new BadRequestException("La cita seleccionada no corresponde a la mascota " + mascota.getNombreMascota() + ".");
        }
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw new ConflictException("Solo se puede registrar la historia clínica de una cita ATENDIDA. "
                    + "La cita está en estado " + cita.getEstado() + ".");
        }
        if (historiaMedicaRepository.existsByCitaCitaId(cita.getCitaId())) {
            throw new ConflictException("La cita seleccionada ya tiene una historia clínica registrada.");
        }

        // Paso 4. La fecha de la consulta debe estar entre el día de la cita y el momento actual.
        LocalDateTime fechaConsulta = rq.getFechaConsulta().truncatedTo(ChronoUnit.MINUTES);
        if (fechaConsulta.toLocalDate().isBefore(cita.getFechaCita().toLocalDate())) {
            throw new BadRequestException("La fecha de la consulta no puede ser anterior a la fecha de la cita.");
        }
        if (fechaConsulta.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de la consulta no puede ser futura.");
        }

        // Paso 5. Guardar.
        HistoriaMedica historia = new HistoriaMedica();
        historia.setCita(cita);
        historia.setMascota(mascota);
        historia.setMedico(medico);
        historia.setFechaConsulta(fechaConsulta);
        historia.setMotivoConsulta(motivo);
        historia.setDiagnostico(diagnostico);
        historia.setObservaciones(observaciones);
        historia.setEstado(EstadoHistoria.ABIERTA);
        historia.setFechaCreacion(LocalDateTime.now());
        historiaMedicaRepository.save(historia);

        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Historia clínica creada correctamente.",
                historia.getHistoriaId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS cerrarHistoria(Long historiaId) {
        HistoriaMedica historia = validador.obtenerHistoria(historiaId);
        if (historia.getEstado() == EstadoHistoria.CERRADA) {
            throw new ConflictException("La historia clínica ya está cerrada.");
        }
        historia.setEstado(EstadoHistoria.CERRADA);
        historia.setFechaModificacion(LocalDateTime.now());
        historiaMedicaRepository.save(historia);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Historia clínica cerrada correctamente.", historiaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistoriaMedica> listarPorMascota(Long mascotaId) {
        validador.obtenerMascota(mascotaId);
        return historiaMedicaRepository.findByMascotaMascotaIdOrderByFechaConsultaDesc(mascotaId);
    }

    /**
     * Convierte el texto del filtro de estado en el enum correspondiente.
     *
     * @param estado texto recibido (puede ser vacío).
     * @return estado, o null si no se filtra por estado.
     * @throws BadRequestException si el texto no es un estado válido.
     */
    private EstadoHistoria convertirEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoHistoria.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El estado '" + estado + "' no es válido. Valores permitidos: ABIERTA, CERRADA.");
        }
    }
}
