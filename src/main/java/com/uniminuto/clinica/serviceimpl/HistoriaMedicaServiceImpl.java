package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.AnotacionRs;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import com.uniminuto.clinica.util.Estados;
import com.uniminuto.clinica.util.NombreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Lógica de negocio de las historias clínicas y sus anotaciones.
 *
 * Reglas: la historia nace de una cita ATENDIDA de la misma mascota; una cita genera máximo una
 * historia; la mascota pertenece al cliente; una historia cerrada no admite anotaciones.
 */
@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    /** Longitud máxima del motivo de consulta. */
    private static final int MAX_MOTIVO = 200;
    /** Longitud máxima de diagnóstico, observaciones y textos de anotaciones. */
    private static final int MAX_TEXTO = 500;

    /** Acceso a datos de historias. */
    @Autowired
    private HistoriaMedicaRepository historiaRepository;

    /** Acceso a datos de anotaciones. */
    @Autowired
    private AnotacionHistoriaRepository anotacionRepository;

    /** Acceso a datos de citas. */
    @Autowired
    private CitaRepository citaRepository;

    /** Acceso a datos de clientes. */
    @Autowired
    private ClienteRepository clienteRepository;

    /** Acceso a datos de mascotas. */
    @Autowired
    private MascotaRepository mascotaRepository;

    /** Acceso a datos de veterinarios. */
    @Autowired
    private MedicoRepository medicoRepository;

    /** {@inheritDoc} */
    @Override
    public List<HistoriaMedicaRs> listar() {
        return historiaRepository.findAllByOrderByFechaConsultaDesc().stream().map(this::aRespuesta).toList();
    }

    /** {@inheritDoc} */
    @Override
    public HistoriaMedicaRs consultar(Long id) {
        return aRespuesta(buscarHistoria(id));
    }

    /** {@inheritDoc} */
    @Override
    public List<HistoriaMedicaRs> listarPorMascota(Long mascotaId) {
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new NotFoundException("La mascota solicitada no fue encontrada.");
        }
        return historiaRepository.findByMascota_MascotaIdOrderByFechaConsultaDesc(mascotaId)
                .stream().map(this::aRespuesta).toList();
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS crear(HistoriaMedicaRq rq) {
        validarCamposHistoria(rq);

        Cliente cliente = clienteRepository.findById(rq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente seleccionado no existe."));
        Mascota mascota = mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota seleccionada no existe."));
        if (mascota.getCliente() == null || !cliente.getId().equals(mascota.getCliente().getId())) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente indicado.");
        }

        Cita cita = citaRepository.findById(rq.getCitaId())
                .orElseThrow(() -> new BadRequestException("La cita seleccionada no existe."));
        if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La cita seleccionada pertenece a otra mascota.");
        }
        if (!Estados.ATENDIDA.equals(cita.getEstado())) {
            throw new ConflictException("Solo una cita en estado Atendida puede generar una historia clínica.");
        }
        if (historiaRepository.existsByCita_Id(cita.getId())) {
            throw new ConflictException("La cita seleccionada ya tiene una historia clínica asociada.");
        }

        Medico medico = obtenerMedico(rq.getMedicoId());
        if (minutos(rq.getFechaHora()).isBefore(minutos(cita.getFecha()))) {
            throw new BadRequestException("La fecha de la consulta no puede ser anterior a la fecha de la cita.");
        }

        HistoriaMedica historia = new HistoriaMedica();
        historia.setMascota(mascota);
        historia.setCita(cita);
        historia.setMedico(medico);
        historia.setFechaConsulta(rq.getFechaHora());
        historia.setFechaCreacion(LocalDateTime.now());
        historia.setMotivoConsulta(rq.getMotivoConsulta().trim());
        historia.setDiagnostico(rq.getDiagnostico().trim());
        historia.setObservaciones(limpiar(rq.getObservaciones()));
        historia.setEstado(rq.getEstado());
        historiaRepository.save(historia);
        return respuesta(201, "Historia clínica registrada correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS cerrar(Long id) {
        HistoriaMedica historia = buscarHistoria(id);
        if (Estados.CERRADA.equals(historia.getEstado())) {
            throw new ConflictException("La historia clínica ya está cerrada.");
        }
        historia.setEstado(Estados.CERRADA);
        historiaRepository.save(historia);
        return respuesta(200, "Historia clínica cerrada correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    public List<AnotacionRs> listarAnotaciones(Long historiaId) {
        buscarHistoria(historiaId);
        return anotacionRepository.findByHistoriaMedica_IdOrderByFechaDesc(historiaId)
                .stream().map(this::aRespuesta).toList();
    }

    /** {@inheritDoc} */
    @Override
    public AnotacionRs consultarAnotacion(Long anotacionId) {
        AnotacionHistoria anotacion = anotacionRepository.findById(anotacionId)
                .orElseThrow(() -> new NotFoundException("La anotación solicitada no fue encontrada."));
        return aRespuesta(anotacion);
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS crearAnotacion(Long historiaId, AnotacionRq rq) {
        HistoriaMedica historia = buscarHistoria(historiaId);
        if (Estados.CERRADA.equals(historia.getEstado())) {
            throw new ConflictException("No se pueden agregar anotaciones a una historia clínica cerrada.");
        }
        validarCamposAnotacion(rq);
        Medico medico = obtenerMedico(rq.getMedicoId());
        if (historia.getFechaConsulta() != null
                && minutos(rq.getFechaHora()).isBefore(minutos(historia.getFechaConsulta()))) {
            throw new BadRequestException("La fecha de la anotación no puede ser anterior a la fecha de la consulta.");
        }

        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoriaMedica(historia);
        anotacion.setMedico(medico);
        anotacion.setFecha(rq.getFechaHora());
        anotacion.setDescripcion(rq.getDescripcion().trim());
        anotacion.setObservacion(limpiar(rq.getObservacion()));
        anotacion.setDiagnosticoEvolucion(rq.getDiagnosticoEvolucion().trim());
        anotacionRepository.save(anotacion);
        return respuesta(201, "Anotación registrada correctamente.");
    }

    /**
     * Busca una historia o lanza 404 si no existe.
     *
     * @param id identificador de la historia.
     * @return la historia encontrada.
     */
    private HistoriaMedica buscarHistoria(Long id) {
        return historiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La historia clínica solicitada no fue encontrada."));
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
     * Valida los datos obligatorios de una historia clínica.
     *
     * @param rq datos recibidos.
     */
    private void validarCamposHistoria(HistoriaMedicaRq rq) {
        if (rq == null) {
            throw new BadRequestException("No se recibieron los datos de la historia clínica.");
        }
        if (rq.getClienteId() == null) {
            throw new BadRequestException("El cliente es obligatorio.");
        }
        if (rq.getMascotaId() == null) {
            throw new BadRequestException("La mascota es obligatoria.");
        }
        if (rq.getCitaId() == null) {
            throw new BadRequestException("La cita atendida es obligatoria.");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio.");
        }
        if (rq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y la hora de la consulta son obligatorias.");
        }
        exigirTexto(rq.getMotivoConsulta(), "el motivo de consulta", MAX_MOTIVO);
        exigirTexto(rq.getDiagnostico(), "el diagnóstico", MAX_TEXTO);
        if (rq.getObservaciones() != null && rq.getObservaciones().length() > MAX_TEXTO) {
            throw new BadRequestException("Las observaciones no pueden superar los " + MAX_TEXTO + " caracteres.");
        }
        if (rq.getEstado() == null || !Estados.HISTORIA_VALIDOS.contains(rq.getEstado())) {
            throw new BadRequestException("El estado de la historia clínica no es válido.");
        }
    }

    /**
     * Valida los datos obligatorios de una anotación.
     *
     * @param rq datos recibidos.
     */
    private void validarCamposAnotacion(AnotacionRq rq) {
        if (rq == null) {
            throw new BadRequestException("No se recibieron los datos de la anotación.");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio.");
        }
        if (rq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y la hora de la anotación son obligatorias.");
        }
        exigirTexto(rq.getDescripcion(), "la descripción", MAX_TEXTO);
        exigirTexto(rq.getDiagnosticoEvolucion(), "el diagnóstico o evolución", MAX_TEXTO);
        if (rq.getObservacion() != null && rq.getObservacion().length() > MAX_TEXTO) {
            throw new BadRequestException("La observación no puede superar los " + MAX_TEXTO + " caracteres.");
        }
    }

    /**
     * Exige que un texto no esté vacío ni supere un máximo.
     *
     * @param texto valor recibido.
     * @param nombre nombre del campo con su artículo en minúscula (ej. "la descripción"), para el mensaje.
     * @param maximo longitud máxima permitida.
     */
    private void exigirTexto(String texto, String nombre, int maximo) {
        if (texto == null || texto.isBlank()) {
            throw new BadRequestException("Debe indicar " + nombre + ".");
        }
        if (texto.trim().length() > maximo) {
            throw new BadRequestException(nombre.substring(0, 1).toUpperCase() + nombre.substring(1)
                    + " no puede superar los " + maximo + " caracteres.");
        }
    }

    /**
     * Trunca una fecha al minuto para compararla sin segundos.
     *
     * @param fecha fecha original.
     * @return fecha truncada.
     */
    private LocalDateTime minutos(LocalDateTime fecha) {
        return fecha.truncatedTo(ChronoUnit.MINUTES);
    }

    /**
     * Recorta un texto y devuelve null si queda vacío.
     *
     * @param texto texto original.
     * @return texto limpio o null.
     */
    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
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
     * Convierte una historia en su DTO resumido (tolera historias antiguas sin cita ni veterinario).
     *
     * @param historia entidad.
     * @return DTO de respuesta.
     */
    private HistoriaMedicaRs aRespuesta(HistoriaMedica historia) {
        HistoriaMedicaRs rs = new HistoriaMedicaRs();
        rs.setId(historia.getId());
        rs.setFechaHora(historia.getFechaConsulta());
        Mascota mascota = historia.getMascota();
        rs.setMascotaId(mascota.getMascotaId());
        rs.setMascotaNombre(mascota.getNombreMascota());
        if (mascota.getCliente() != null) {
            rs.setClienteId(mascota.getCliente().getId());
            rs.setClienteNombre(NombreUtil.completo(mascota.getCliente()));
        }
        if (historia.getMedico() != null) {
            rs.setMedicoId(historia.getMedico().getId());
            rs.setMedicoNombre(NombreUtil.completo(historia.getMedico()));
        }
        if (historia.getCita() != null) {
            rs.setCitaId(historia.getCita().getId());
        }
        rs.setMotivoConsulta(historia.getMotivoConsulta());
        rs.setDiagnostico(historia.getDiagnostico());
        rs.setObservaciones(historia.getObservaciones());
        rs.setEstado(historia.getEstado());
        return rs;
    }

    /**
     * Convierte una anotación en su DTO.
     *
     * @param anotacion entidad.
     * @return DTO de respuesta.
     */
    private AnotacionRs aRespuesta(AnotacionHistoria anotacion) {
        AnotacionRs rs = new AnotacionRs();
        rs.setId(anotacion.getId());
        rs.setHistoriaId(anotacion.getHistoriaMedica().getId());
        rs.setFechaHora(anotacion.getFecha());
        if (anotacion.getMedico() != null) {
            rs.setMedicoId(anotacion.getMedico().getId());
            rs.setMedicoNombre(NombreUtil.completo(anotacion.getMedico()));
        }
        rs.setDescripcion(anotacion.getDescripcion());
        rs.setObservacion(anotacion.getObservacion());
        rs.setDiagnosticoEvolucion(anotacion.getDiagnosticoEvolucion());
        return rs;
    }
}
