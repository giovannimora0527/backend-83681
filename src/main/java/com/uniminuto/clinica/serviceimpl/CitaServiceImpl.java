package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.Estados;
import com.uniminuto.clinica.util.NombreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/**
 * Lógica de negocio de las citas médicas.
 *
 * Reglas críticas validadas aquí (no solo en Angular): cliente, mascota y veterinario existentes;
 * la mascota pertenece al cliente; no hay citas en el pasado; un veterinario no tiene dos citas
 * dentro de un margen de 15 minutos; una mascota no tiene citas simultáneas; y el estado limita
 * qué se puede modificar (cancelada: nada; atendida: solo observaciones).
 */
@Service
public class CitaServiceImpl implements CitaService {

    /** Margen en minutos, antes y después, que debe respetarse entre citas de un veterinario o mascota. */
    private static final long MARGEN_MINUTOS = 15;
    /** Longitud máxima del motivo. */
    private static final int MAX_MOTIVO = 200;
    /** Longitud máxima de las observaciones. */
    private static final int MAX_OBSERVACIONES = 255;

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
    public List<CitaRs> listar() {
        return citaRepository.findAllByOrderByFechaDesc().stream().map(this::aRespuesta).toList();
    }

    /** {@inheritDoc} */
    @Override
    public CitaRs consultar(Long id) {
        return aRespuesta(buscarCita(id));
    }

    /** {@inheritDoc} */
    @Override
    public List<CitaRs> listarPorMascota(Long mascotaId) {
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new NotFoundException("La mascota solicitada no fue encontrada.");
        }
        return citaRepository.findByMascota_MascotaIdOrderByFechaDesc(mascotaId).stream().map(this::aRespuesta).toList();
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS crear(CitaRq rq) {
        validarCampos(rq);
        if (!Estados.CITA_NUEVA.contains(rq.getEstado())) {
            throw new BadRequestException("Una cita nueva solo puede registrarse como Pendiente, Confirmada o Programada.");
        }
        Mascota mascota = validarClienteYMascota(rq);
        Medico medico = obtenerMedico(rq.getMedicoId());
        exigirFechaFutura(rq.getFechaHora());
        validarDisponibilidad(null, mascota.getMascotaId(), medico.getId(), rq.getFechaHora());

        Cita cita = new Cita();
        asignar(cita, rq, mascota, medico);
        citaRepository.save(cita);
        return respuesta(201, "Cita registrada correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS editar(Long id, CitaRq rq) {
        Cita cita = buscarCita(id);
        validarCampos(rq);

        if (Estados.CANCELADA.equals(cita.getEstado())) {
            throw new ConflictException("Una cita cancelada no puede modificarse. Si desea usarla de nuevo, reactívela.");
        }
        if (Estados.ATENDIDA.equals(cita.getEstado())) {
            exigirSoloObservaciones(cita, rq);
            cita.setObservaciones(limpiar(rq.getObservaciones()));
            citaRepository.save(cita);
            return respuesta(200, "Observaciones de la cita actualizadas correctamente.");
        }

        Mascota mascota = validarClienteYMascota(rq);
        Medico medico = obtenerMedico(rq.getMedicoId());
        if (!iguales(rq.getFechaHora(), cita.getFecha())) {
            exigirFechaFutura(rq.getFechaHora());
        }
        if (!Estados.CANCELADA.equals(rq.getEstado())) {
            validarDisponibilidad(cita.getId(), mascota.getMascotaId(), medico.getId(), rq.getFechaHora());
        }
        asignar(cita, rq, mascota, medico);
        citaRepository.save(cita);
        return respuesta(200, "Cita actualizada correctamente.");
    }

    /** {@inheritDoc} */
    @Override
    public MiRespuestaRS reactivar(Long id) {
        Cita cita = buscarCita(id);
        if (!Estados.CANCELADA.equals(cita.getEstado())) {
            throw new ConflictException("Solo se pueden reactivar citas canceladas.");
        }
        if (cita.getFecha().isBefore(LocalDateTime.now())) {
            throw new ConflictException("No se puede reactivar una cita cuya fecha ya pasó. Registre una nueva cita.");
        }
        validarDisponibilidad(cita.getId(), cita.getMascota().getMascotaId(), cita.getMedico().getId(), cita.getFecha());
        cita.setEstado(Estados.PENDIENTE);
        citaRepository.save(cita);
        return respuesta(200, "Cita reactivada correctamente. Quedó en estado Pendiente.");
    }

    /**
     * Busca una cita o lanza 404 si no existe.
     *
     * @param id identificador de la cita.
     * @return la cita encontrada.
     */
    private Cita buscarCita(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La cita solicitada no fue encontrada."));
    }

    /**
     * Valida que los datos obligatorios estén presentes y dentro de los límites permitidos.
     *
     * @param rq datos recibidos.
     */
    private void validarCampos(CitaRq rq) {
        if (rq == null) {
            throw new BadRequestException("No se recibieron los datos de la cita.");
        }
        if (rq.getClienteId() == null) {
            throw new BadRequestException("El cliente es obligatorio.");
        }
        if (rq.getMascotaId() == null) {
            throw new BadRequestException("La mascota es obligatoria.");
        }
        if (rq.getMedicoId() == null) {
            throw new BadRequestException("El veterinario es obligatorio.");
        }
        if (rq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y la hora de la cita son obligatorias.");
        }
        if (rq.getMotivo() == null || rq.getMotivo().isBlank()) {
            throw new BadRequestException("El motivo de la consulta es obligatorio.");
        }
        if (rq.getMotivo().trim().length() > MAX_MOTIVO) {
            throw new BadRequestException("El motivo no puede superar los " + MAX_MOTIVO + " caracteres.");
        }
        if (rq.getObservaciones() != null && rq.getObservaciones().length() > MAX_OBSERVACIONES) {
            throw new BadRequestException("Las observaciones no pueden superar los " + MAX_OBSERVACIONES + " caracteres.");
        }
        if (rq.getEstado() == null || !Estados.CITA_VALIDOS.contains(rq.getEstado())) {
            throw new BadRequestException("El estado de la cita no es válido.");
        }
    }

    /**
     * Valida que el cliente y la mascota existan y que la mascota pertenezca a ese cliente.
     *
     * @param rq datos recibidos.
     * @return la mascota validada.
     */
    private Mascota validarClienteYMascota(CitaRq rq) {
        Cliente cliente = clienteRepository.findById(rq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente seleccionado no existe."));
        Mascota mascota = mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota seleccionada no existe."));
        if (mascota.getCliente() == null || !cliente.getId().equals(mascota.getCliente().getId())) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente indicado.");
        }
        return mascota;
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
     * Exige que la fecha y hora no sean anteriores al momento actual.
     *
     * @param fechaHora fecha y hora propuestas.
     */
    private void exigirFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("No se pueden registrar citas en fechas u horas anteriores al momento actual.");
        }
    }

    /**
     * Valida que no exista una cita duplicada, que el veterinario esté disponible y que la mascota no
     * tenga otra cita simultánea (margen de 15 minutos antes y después; las canceladas no ocupan horario).
     *
     * @param citaActualId cita que se está editando (se ignora a sí misma); null al crear.
     * @param mascotaId mascota de la cita.
     * @param medicoId veterinario de la cita.
     * @param fechaHora fecha y hora propuestas.
     */
    private void validarDisponibilidad(Long citaActualId, Long mascotaId, Long medicoId, LocalDateTime fechaHora) {
        LocalDateTime desde = fechaHora.minusMinutes(MARGEN_MINUTOS);
        LocalDateTime hasta = fechaHora.plusMinutes(MARGEN_MINUTOS);

        List<Cita> delVeterinario = citaRepository
                .findByMedico_IdAndFechaBetweenAndEstadoNot(medicoId, desde, hasta, Estados.CANCELADA)
                .stream().filter(c -> !c.getId().equals(citaActualId)).toList();

        boolean duplicada = delVeterinario.stream().anyMatch(c ->
                c.getMascota().getMascotaId().equals(mascotaId) && iguales(c.getFecha(), fechaHora));
        if (duplicada) {
            throw new ConflictException("Cita duplicada: ya existe una cita de esta mascota con este veterinario en la misma fecha y hora.");
        }
        if (!delVeterinario.isEmpty()) {
            throw new ConflictException("Veterinario no disponible: ya tiene una cita muy cercana a ese horario (se requieren 15 minutos antes y después).");
        }

        boolean mascotaOcupada = citaRepository
                .findByMascota_MascotaIdAndFechaBetweenAndEstadoNot(mascotaId, desde, hasta, Estados.CANCELADA)
                .stream().anyMatch(c -> !c.getId().equals(citaActualId));
        if (mascotaOcupada) {
            throw new ConflictException("La mascota ya tiene otra cita en ese horario. Una mascota no puede tener citas simultáneas.");
        }
    }

    /**
     * Una cita atendida es información histórica: solo se permite cambiar las observaciones.
     *
     * @param cita cita almacenada.
     * @param rq datos recibidos.
     */
    private void exigirSoloObservaciones(Cita cita, CitaRq rq) {
        boolean cambio = !cita.getMascota().getMascotaId().equals(rq.getMascotaId())
                || !Objects.equals(clienteDe(cita.getMascota()), rq.getClienteId())
                || !cita.getMedico().getId().equals(rq.getMedicoId())
                || !iguales(cita.getFecha(), rq.getFechaHora())
                || !cita.getMotivo().trim().equals(rq.getMotivo().trim())
                || !cita.getEstado().equals(rq.getEstado());
        if (cambio) {
            throw new ConflictException("Una cita atendida forma parte del historial clínico: solo se pueden modificar las observaciones.");
        }
    }

    /**
     * Identificador del cliente dueño de una mascota (null si no tiene cliente).
     *
     * @param mascota mascota.
     * @return id del cliente o null.
     */
    private Long clienteDe(Mascota mascota) {
        return mascota.getCliente() == null ? null : mascota.getCliente().getId();
    }

    /**
     * Copia los datos recibidos a la entidad.
     *
     * @param cita entidad destino.
     * @param rq datos recibidos.
     * @param mascota mascota validada.
     * @param medico veterinario validado.
     */
    private void asignar(Cita cita, CitaRq rq, Mascota mascota, Medico medico) {
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFecha(rq.getFechaHora());
        cita.setMotivo(rq.getMotivo().trim());
        cita.setEstado(rq.getEstado());
        cita.setObservaciones(limpiar(rq.getObservaciones()));
    }

    /**
     * Compara dos fechas ignorando segundos y fracciones.
     *
     * @param a primera fecha.
     * @param b segunda fecha.
     * @return true si coinciden hasta el minuto.
     */
    private boolean iguales(LocalDateTime a, LocalDateTime b) {
        return a.truncatedTo(ChronoUnit.MINUTES).equals(b.truncatedTo(ChronoUnit.MINUTES));
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
     * Convierte la entidad en el DTO resumido para el listado.
     *
     * @param cita entidad.
     * @return DTO de respuesta.
     */
    private CitaRs aRespuesta(Cita cita) {
        CitaRs rs = new CitaRs();
        rs.setId(cita.getId());
        rs.setFechaHora(cita.getFecha());
        Mascota mascota = cita.getMascota();
        rs.setMascotaId(mascota.getMascotaId());
        rs.setMascotaNombre(mascota.getNombreMascota());
        if (mascota.getCliente() != null) {
            rs.setClienteId(mascota.getCliente().getId());
            rs.setClienteNombre(NombreUtil.completo(mascota.getCliente()));
        }
        rs.setMedicoId(cita.getMedico().getId());
        rs.setMedicoNombre(NombreUtil.completo(cita.getMedico()));
        rs.setMotivo(cita.getMotivo());
        rs.setEstado(cita.getEstado());
        rs.setObservaciones(cita.getObservaciones());
        return rs;
    }
}
