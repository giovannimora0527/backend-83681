package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.EstadoCita;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.CambioEstadoCitaRq;
import com.uniminuto.clinica.models.CitaFiltroRq;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRs;
import com.uniminuto.clinica.models.ReactivarCitaRq;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.specification.CitaSpecification;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.PaginacionUtil;
import com.uniminuto.clinica.util.ValidacionUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementación del servicio de citas médicas.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /**
     * Minutos antes y después de una cita en los que el veterinario (o la
     * mascota) no puede tener otra cita.
     */
    public static final int MINUTOS_SEPARACION = 15;

    /**
     * Longitud máxima del motivo de la cita.
     */
    private static final int LONGITUD_MOTIVO = 255;

    /**
     * Formato de hora usado en los mensajes de error.
     */
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Columnas por las que se puede ordenar el listado (columna del frontend → propiedad).
     */
    private static final Map<String, String> COLUMNAS_ORDEN = Map.of(
            "fecha", "fechaCita",
            "cliente", "mascota.cliente.nombres",
            "mascota", "mascota.nombreMascota",
            "veterinario", "medico.nombres",
            "motivo", "motivo",
            "estado", "estado");

    /**
     * Repositorio de citas.
     */
    private final CitaRepository citaRepository;

    /**
     * Validador de las entidades relacionadas (cliente, mascota, veterinario).
     */
    private final ValidadorRelaciones validador;

    public CitaServiceImpl(CitaRepository citaRepository, ValidadorRelaciones validador) {
        this.citaRepository = citaRepository;
        this.validador = validador;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PaginaRs<Cita> listarCitas(CitaFiltroRq filtro) {
        EstadoCita estado = filtro.getEstado() == null || filtro.getEstado().isBlank()
                ? null : EstadoCita.desdeTexto(filtro.getEstado());
        Pageable pageable = PaginacionUtil.construir(filtro, COLUMNAS_ORDEN, "fecha");
        return PaginaRs.desde(citaRepository.findAll(CitaSpecification.conFiltros(filtro, estado), pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Cita consultarCita(Long citaId) {
        return validador.obtenerCita(citaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS crearCita(CitaRq citaRq) {
        // Paso 1. Validar los campos obligatorios y el estado inicial.
        validarCamposObligatorios(citaRq);
        EstadoCita estado = EstadoCita.desdeTexto(citaRq.getEstado());
        if (!EstadoCita.ESTADOS_INICIALES.contains(estado)) {
            throw new BadRequestException("Una cita nueva solo puede registrarse como PENDIENTE, PROGRAMADA o CONFIRMADA.");
        }

        // Paso 2. Validar que cliente, mascota y veterinario existan y sean coherentes.
        Mascota mascota = validador.obtenerMascotaDeCliente(citaRq.getMascotaId(), citaRq.getClienteId());
        Medico medico = validador.obtenerMedico(citaRq.getMedicoId());

        // Paso 3. Validar la fecha y la disponibilidad de agenda.
        validarFechaFutura(citaRq.getFechaCita());
        validarDisponibilidad(medico, mascota, citaRq.getFechaCita(), null);

        // Paso 4. Guardar la cita.
        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaCita(truncarSegundos(citaRq.getFechaCita()));
        cita.setMotivo(ValidacionUtil.textoObligatorio(citaRq.getMotivo(), "motivo", LONGITUD_MOTIVO));
        cita.setEstado(estado);
        cita.setFechaCreacion(LocalDateTime.now());
        citaRepository.save(cita);

        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Cita creada correctamente.", cita.getCitaId());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Restricciones: la mascota (y por lo tanto el cliente) de una cita no se
     * puede cambiar, porque la cita es de ese paciente; si se necesita otro
     * paciente se debe crear otra cita. El estado tampoco se cambia aquí, sino
     * con {@link #cambiarEstado(Long, CambioEstadoCitaRq)}.
     */
    @Override
    @Transactional
    public MiRespuestaRS editarCita(Long citaId, CitaRq citaRq) {
        // Paso 1. Validar que la cita exista y que su estado permita editarla.
        Cita cita = validador.obtenerCita(citaId);
        validarCitaModificable(cita);
        validarCamposObligatorios(citaRq);

        // Paso 2. La mascota y el cliente de la cita no se pueden cambiar.
        if (!Objects.equals(cita.getMascota().getMascotaId(), citaRq.getMascotaId())
                || !Objects.equals(cita.getMascota().getCliente().getId(), citaRq.getClienteId())) {
            throw new BadRequestException("No es posible cambiar el cliente o la mascota de una cita. "
                    + "Cancele esta cita y registre una nueva.");
        }

        // Paso 3. Validar veterinario, fecha y disponibilidad (excluyendo la propia cita).
        Medico medico = validador.obtenerMedico(citaRq.getMedicoId());
        LocalDateTime nuevaFecha = truncarSegundos(citaRq.getFechaCita());
        boolean cambioFecha = !nuevaFecha.equals(cita.getFechaCita());
        boolean cambioMedico = !Objects.equals(medico.getId(), cita.getMedico().getId());
        if (cambioFecha) {
            validarFechaFutura(nuevaFecha);
        }
        if (cambioFecha || cambioMedico) {
            validarDisponibilidad(medico, cita.getMascota(), nuevaFecha, cita.getCitaId());
        }

        // Paso 4. Guardar los cambios.
        cita.setMedico(medico);
        cita.setFechaCita(nuevaFecha);
        cita.setMotivo(ValidacionUtil.textoObligatorio(citaRq.getMotivo(), "motivo", LONGITUD_MOTIVO));
        cita.setFechaModificacion(LocalDateTime.now());
        citaRepository.save(cita);

        return new MiRespuestaRS(HttpStatus.OK.value(), "Cita actualizada correctamente.", cita.getCitaId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS cambiarEstado(Long citaId, CambioEstadoCitaRq rq) {
        Cita cita = validador.obtenerCita(citaId);
        ValidacionUtil.obligatorio(rq, "estado");
        EstadoCita nuevoEstado = EstadoCita.desdeTexto(rq.getEstado());
        EstadoCita estadoActual = cita.getEstado();

        if (estadoActual == nuevoEstado) {
            throw new ConflictException("La cita ya se encuentra en estado " + estadoActual + ".");
        }
        if (estadoActual == EstadoCita.CANCELADA) {
            throw new ConflictException("La cita está cancelada. Para usarla de nuevo debe reactivarla.");
        }
        if (!estadoActual.puedeCambiarA(nuevoEstado)) {
            throw new ConflictException("No se puede cambiar una cita de " + estadoActual + " a " + nuevoEstado + ".");
        }
        // Una cita solo se puede atender el día programado o después, nunca antes.
        if (nuevoEstado == EstadoCita.ATENDIDA && cita.getFechaCita().toLocalDate().isAfter(LocalDate.now())) {
            throw new ConflictException("No se puede marcar como atendida una cita programada para una fecha futura.");
        }

        cita.setEstado(nuevoEstado);
        cita.setFechaModificacion(LocalDateTime.now());
        citaRepository.save(cita);
        return new MiRespuestaRS(HttpStatus.OK.value(), "La cita quedó en estado " + nuevoEstado + ".", cita.getCitaId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MiRespuestaRS reactivarCita(Long citaId, ReactivarCitaRq rq) {
        Cita cita = validador.obtenerCita(citaId);
        if (cita.getEstado() != EstadoCita.CANCELADA) {
            throw new ConflictException("Solo se pueden reactivar citas canceladas.");
        }

        LocalDateTime fecha = rq != null && rq.getFechaCita() != null
                ? truncarSegundos(rq.getFechaCita()) : cita.getFechaCita();
        validarFechaFutura(fecha);
        validarDisponibilidad(cita.getMedico(), cita.getMascota(), fecha, cita.getCitaId());

        cita.setFechaCita(fecha);
        cita.setEstado(EstadoCita.PENDIENTE);
        cita.setFechaModificacion(LocalDateTime.now());
        citaRepository.save(cita);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Cita reactivada correctamente en estado PENDIENTE.",
                cita.getCitaId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarAtendidasSinHistoria(Long mascotaId) {
        validador.obtenerMascota(mascotaId);
        return citaRepository.buscarAtendidasSinHistoria(mascotaId, EstadoCita.ATENDIDA);
    }

    /**
     * Valida los campos obligatorios de la solicitud.
     *
     * @param citaRq solicitud a validar.
     * @throws BadRequestException si falta algún dato.
     */
    private void validarCamposObligatorios(CitaRq citaRq) {
        ValidacionUtil.obligatorio(citaRq, "cita");
        ValidacionUtil.obligatorio(citaRq.getClienteId(), "cliente");
        ValidacionUtil.obligatorio(citaRq.getMascotaId(), "mascota");
        ValidacionUtil.obligatorio(citaRq.getMedicoId(), "veterinario");
        ValidacionUtil.obligatorio(citaRq.getFechaCita(), "fecha y hora");
        ValidacionUtil.textoObligatorio(citaRq.getMotivo(), "motivo", LONGITUD_MOTIVO);
    }

    /**
     * Valida que el estado actual de la cita permita modificar su información.
     *
     * @param cita cita a revisar.
     * @throws ConflictException si la cita está cancelada o atendida.
     */
    private void validarCitaModificable(Cita cita) {
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new ConflictException("La cita está cancelada y no se puede modificar. "
                    + "Si necesita usarla de nuevo, reactívela.");
        }
        if (cita.getEstado() == EstadoCita.ATENDIDA) {
            throw new ConflictException("La cita ya fue atendida; su información histórica no se puede modificar.");
        }
    }

    /**
     * Valida que la fecha de la cita no sea anterior al momento actual.
     *
     * @param fecha fecha y hora de la cita.
     * @throws BadRequestException si la fecha ya pasó.
     */
    private void validarFechaFutura(LocalDateTime fecha) {
        if (fecha.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("No se pueden registrar citas en fechas u horas anteriores a la actual.");
        }
    }

    /**
     * Valida que ni el veterinario ni la mascota tengan otra cita activa a
     * menos de {@link #MINUTOS_SEPARACION} minutos de la fecha indicada.
     *
     * @param medico        veterinario de la cita.
     * @param mascota       mascota de la cita.
     * @param fecha         fecha y hora de la cita.
     * @param citaIdExcluir cita que se está editando (null al crear).
     * @throws ConflictException si existe un cruce de horario.
     */
    private void validarDisponibilidad(Medico medico, Mascota mascota, LocalDateTime fecha, Long citaIdExcluir) {
        LocalDateTime desde = fecha.minusMinutes(MINUTOS_SEPARACION);
        LocalDateTime hasta = fecha.plusMinutes(MINUTOS_SEPARACION);

        List<Cita> crucesMedico = citaRepository.buscarCrucesMedico(medico.getId(), desde, hasta,
                citaIdExcluir, EstadoCita.CANCELADA);
        if (!crucesMedico.isEmpty()) {
            throw new ConflictException("El veterinario " + medico.getNombres() + " " + medico.getApellidos()
                    + " no está disponible: ya tiene una cita el "
                    + crucesMedico.get(0).getFechaCita().format(FORMATO_FECHA_HORA)
                    + ". Debe haber al menos " + MINUTOS_SEPARACION + " minutos entre citas.");
        }

        List<Cita> crucesMascota = citaRepository.buscarCrucesMascota(mascota.getMascotaId(), desde, hasta,
                citaIdExcluir, EstadoCita.CANCELADA);
        if (!crucesMascota.isEmpty()) {
            throw new ConflictException("La mascota " + mascota.getNombreMascota() + " ya tiene una cita el "
                    + crucesMascota.get(0).getFechaCita().format(FORMATO_FECHA_HORA) + ".");
        }
    }

    /**
     * Elimina los segundos y nanosegundos de una fecha, porque las citas se
     * agendan por hora y minuto.
     *
     * @param fecha fecha original.
     * @return fecha sin segundos.
     */
    private LocalDateTime truncarSegundos(LocalDateTime fecha) {
        return fecha.truncatedTo(ChronoUnit.MINUTES);
    }
}
