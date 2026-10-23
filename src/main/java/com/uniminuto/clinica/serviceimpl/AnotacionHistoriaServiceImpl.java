package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.EstadoHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.FiltroPaginadoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.specification.AnotacionHistoriaSpecification;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import com.uniminuto.clinica.util.PaginacionUtil;
import com.uniminuto.clinica.util.ValidacionUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Implementación del servicio de anotaciones de historias clínicas.
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /**
     * Longitud máxima de los campos de texto de la anotación.
     */
    private static final int LONGITUD_TEXTO = 2000;

    /**
     * Columnas por las que se puede ordenar el histórico (columna del frontend → propiedad).
     */
    private static final Map<String, String> COLUMNAS_ORDEN = Map.of(
            "fecha", "fechaAnotacion",
            "veterinario", "medico.nombres");

    /**
     * Repositorio de anotaciones.
     */
    private final AnotacionHistoriaRepository anotacionRepository;

    /**
     * Validador de las entidades relacionadas.
     */
    private final ValidadorRelaciones validador;

    public AnotacionHistoriaServiceImpl(AnotacionHistoriaRepository anotacionRepository,
            ValidadorRelaciones validador) {
        this.anotacionRepository = anotacionRepository;
        this.validador = validador;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS crearAnotacion(AnotacionHistoriaRq rq) {
        // Paso 1. Campos obligatorios.
        ValidacionUtil.obligatorio(rq, "anotación");
        ValidacionUtil.obligatorio(rq.getFechaAnotacion(), "fecha y hora de la anotación");
        String descripcion = ValidacionUtil.textoObligatorio(rq.getDescripcion(), "descripción", LONGITUD_TEXTO);
        String observacion = ValidacionUtil.textoObligatorio(rq.getObservacion(), "observación", LONGITUD_TEXTO);
        String evolucion = ValidacionUtil.textoObligatorio(rq.getDiagnosticoEvolucion(),
                "diagnóstico o evolución", LONGITUD_TEXTO);

        // Paso 2. La historia existe y está abierta; el veterinario existe.
        HistoriaMedica historia = validador.obtenerHistoria(rq.getHistoriaId());
        if (historia.getEstado() == EstadoHistoria.CERRADA) {
            throw new ConflictException("La historia clínica está cerrada y no admite nuevas anotaciones.");
        }
        Medico medico = validador.obtenerMedico(rq.getMedicoId());

        // Paso 3. La fecha debe estar entre la consulta y el momento actual.
        LocalDateTime fecha = rq.getFechaAnotacion().truncatedTo(ChronoUnit.MINUTES);
        if (fecha.isBefore(historia.getFechaConsulta())) {
            throw new BadRequestException("La fecha de la anotación no puede ser anterior a la fecha de la consulta.");
        }
        if (fecha.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de la anotación no puede ser futura.");
        }

        // Paso 4. Guardar.
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoriaMedica(historia);
        anotacion.setMedico(medico);
        anotacion.setFechaAnotacion(fecha);
        anotacion.setDescripcion(descripcion);
        anotacion.setObservacion(observacion);
        anotacion.setDiagnosticoEvolucion(evolucion);
        anotacion.setFechaCreacion(LocalDateTime.now());
        anotacionRepository.save(anotacion);

        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Anotación agregada correctamente.",
                anotacion.getAnotacionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<AnotacionHistoria> listarPorHistoria(Long historiaId, FiltroPaginadoRq filtro) {
        validador.obtenerHistoria(historiaId);
        Pageable pageable = PaginacionUtil.construir(filtro, COLUMNAS_ORDEN, "fecha");
        return PaginaRs.desde(anotacionRepository.findAll(
                AnotacionHistoriaSpecification.conFiltros(historiaId, null, filtro), pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<AnotacionHistoria> listarPorMascota(Long mascotaId, FiltroPaginadoRq filtro) {
        validador.obtenerMascota(mascotaId);
        Pageable pageable = PaginacionUtil.construir(filtro, COLUMNAS_ORDEN, "fecha");
        return PaginaRs.desde(anotacionRepository.findAll(
                AnotacionHistoriaSpecification.conFiltros(null, mascotaId, filtro), pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AnotacionHistoria consultarAnotacion(Long anotacionId) {
        return anotacionRepository.findById(anotacionId)
                .orElseThrow(() -> new NotFoundException("La anotación con id " + anotacionId + " no existe."));
    }
}
