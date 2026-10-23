package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.CitaRequest;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.EstadoCita;
import com.uniminuto.clinica.util.RespuestaUtil;
import com.uniminuto.clinica.util.ValidadorEntidades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Logica de negocio de las citas medicas.
 *
 * Reglas implementadas (numeradas como en el enunciado del parcial):
 * 1. No se registra una cita para un cliente inexistente.
 * 2. La mascota debe pertenecer al cliente seleccionado.
 * 3. El veterinario debe existir.
 * 4. Un veterinario no puede tener dos citas con menos de 15 minutos de diferencia.
 * 5. Una mascota no puede tener dos citas simultaneas (misma ventana de 15 minutos).
 * 6. No se registran citas en fechas anteriores a la actual.
 * 7. Estados: PENDIENTE, PROGRAMADA, CONFIRMADA, ATENDIDA, CANCELADA.
 * 8. Una cita cancelada no se modifica; solo se puede reactivar explicitamente.
 * 9. Una cita atendida no se modifica (es historica) y puede generar historia clinica.
 *    Solo se puede marcar ATENDIDA una cita cuya fecha ya llego.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /**
     * Minutos minimos de separacion entre dos citas del mismo veterinario o mascota.
     */
    private static final long MINUTOS_SEPARACION = 15;

    /**
     * Formato de fecha para los mensajes al usuario (ej: 25/10/2026 10:30).
     */
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Repositorio de citas.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Validaciones comunes (cliente, mascota, veterinario, cita).
     */
    @Autowired
    private ValidadorEntidades validador;

    @Override
    public List<CitaMedica> listarCitas() {
        return citaRepository.findAllByOrderByFechaHoraDesc();
    }

    @Override
    public CitaMedica consultarCita(Long citaId) throws BadRequestException {
        return validador.obtenerCita(citaId);
    }

    @Override
    public List<CitaMedica> listarCitasPorMascota(Long mascotaId) throws BadRequestException {
        validador.obtenerMascota(mascotaId);
        return citaRepository.findByMascotaMascotaIdOrderByFechaHoraDesc(mascotaId);
    }

    @Override
    public List<CitaMedica> filtrarCitasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFinal)
            throws BadRequestException {
        if (fechaInicio == null || fechaFinal == null) {
            throw new BadRequestException("Debe indicar la fecha inicial y la fecha final.");
        }
        if (fechaInicio.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return citaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(fechaInicio, fechaFinal);
    }

    @Override
    public MiRespuestaRS crearCita(CitaRequest citaRq) throws BadRequestException {
        // Paso 1. Campos obligatorios
        this.validarCamposObligatorios(citaRq);

        // Paso 2. Reglas 1 y 2: el cliente existe y la mascota es de ese cliente
        Mascota mascota = validador.obtenerMascotaDelCliente(citaRq.getClienteId(), citaRq.getMascotaId());

        // Paso 3. Regla 3: el veterinario existe
        Medico medico = validador.obtenerMedico(citaRq.getMedicoId());

        // Paso 4. Regla 6: la fecha no puede estar en el pasado
        this.validarFechaFutura(citaRq.getFechaHora());

        // Paso 5. Regla 7: estado inicial valido (por defecto PENDIENTE)
        String estado = citaRq.getEstado() == null || citaRq.getEstado().trim().isEmpty()
                ? EstadoCita.PENDIENTE
                : this.normalizarEstado(citaRq.getEstado());
        if (!EstadoCita.INICIALES.contains(estado)) {
            throw new BadRequestException("Una cita nueva solo puede crearse en estado "
                    + String.join(", ", EstadoCita.INICIALES) + ".");
        }

        // Paso 6. Reglas 4 y 5: disponibilidad del veterinario y de la mascota
        this.validarDisponibilidad(medico, mascota, citaRq.getFechaHora(), null);

        // Paso 7. Guardar
        CitaMedica cita = new CitaMedica();
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setMotivo(citaRq.getMotivo().trim());
        cita.setEstado(estado);
        citaRepository.save(cita);

        return RespuestaUtil.crear(201, "Cita creada correctamente para "
                + mascota.getNombreMascota() + " el " + citaRq.getFechaHora().format(FORMATO_FECHA) + ".");
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRequest citaRq) throws BadRequestException {
        // Paso 1. La cita debe existir
        if (citaRq == null || citaRq.getId() == null) {
            throw new BadRequestException("El id de la cita es obligatorio para actualizar.");
        }
        CitaMedica cita = validador.obtenerCita(citaRq.getId());

        // Paso 2. Reglas 8 y 9: segun el estado actual, ¿se puede modificar?
        if (EstadoCita.CANCELADA.equals(cita.getEstado())) {
            throw new ConflictException("La cita está CANCELADA y no puede modificarse. "
                    + "Si desea retomarla, use la opción Reactivar.");
        }
        if (EstadoCita.ATENDIDA.equals(cita.getEstado())) {
            throw new ConflictException("La cita ya fue ATENDIDA; su información es histórica "
                    + "y no puede modificarse.");
        }

        // Paso 3. Campos obligatorios y relaciones
        this.validarCamposObligatorios(citaRq);
        if (!cita.getMascota().getMascotaId().equals(citaRq.getMascotaId())) {
            throw new BadRequestException("No se puede cambiar la mascota de una cita existente. "
                    + "Cancele esta cita y cree una nueva.");
        }
        Mascota mascota = validador.obtenerMascotaDelCliente(citaRq.getClienteId(), citaRq.getMascotaId());
        Medico medico = validador.obtenerMedico(citaRq.getMedicoId());
        String nuevoEstado = this.normalizarEstado(citaRq.getEstado());

        // Una cita solo puede marcarse ATENDIDA si su fecha ya llego
        if (EstadoCita.ATENDIDA.equals(nuevoEstado) && citaRq.getFechaHora().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("No se puede marcar como ATENDIDA una cita cuya fecha aún no ha llegado.");
        }

        // Paso 4. Si cambia la fecha, la nueva fecha no puede estar en el pasado
        boolean cambiaFecha = !cita.getFechaHora().equals(citaRq.getFechaHora());
        boolean cambiaMedico = !cita.getMedico().getId().equals(medico.getId());
        if (cambiaFecha) {
            this.validarFechaFutura(citaRq.getFechaHora());
        }

        // Paso 5. Si cambia la fecha o el veterinario (y no se esta cancelando),
        // se vuelve a revisar la disponibilidad, excluyendo esta misma cita.
        if ((cambiaFecha || cambiaMedico) && !EstadoCita.CANCELADA.equals(nuevoEstado)) {
            this.validarDisponibilidad(medico, mascota, citaRq.getFechaHora(), cita.getId());
        }

        // Paso 6. Guardar cambios
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setMotivo(citaRq.getMotivo().trim());
        cita.setEstado(nuevoEstado);
        citaRepository.save(cita);

        return RespuestaUtil.crear(200, "Cita actualizada correctamente. Estado: " + nuevoEstado + ".");
    }

    @Override
    public MiRespuestaRS reactivarCita(Long citaId) throws BadRequestException {
        CitaMedica cita = validador.obtenerCita(citaId);

        if (!EstadoCita.CANCELADA.equals(cita.getEstado())) {
            throw new ConflictException("Solo se pueden reactivar citas CANCELADAS. Estado actual: "
                    + cita.getEstado() + ".");
        }
        if (cita.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new ConflictException("No se puede reactivar la cita porque su fecha ya pasó. "
                    + "Cree una cita nueva.");
        }
        // Mientras estuvo cancelada, otra cita pudo ocupar ese horario
        this.validarDisponibilidad(cita.getMedico(), cita.getMascota(), cita.getFechaHora(), cita.getId());

        cita.setEstado(EstadoCita.PENDIENTE);
        citaRepository.save(cita);
        return RespuestaUtil.crear(200, "Cita reactivada correctamente. Nuevo estado: PENDIENTE.");
    }

    /**
     * Valida los campos obligatorios de la peticion.
     *
     * @param citaRq datos recibidos.
     * @throws BadRequestException si falta algun campo.
     */
    private void validarCamposObligatorios(CitaRequest citaRq) {
        if (citaRq == null) {
            throw new BadRequestException("Los datos de la cita son obligatorios.");
        }
        validador.validarIdObligatorio(citaRq.getClienteId(), "cliente");
        validador.validarIdObligatorio(citaRq.getMascotaId(), "mascota");
        validador.validarIdObligatorio(citaRq.getMedicoId(), "veterinario");
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("El campo fecha y hora es obligatorio.");
        }
        validador.validarTextoObligatorio(citaRq.getMotivo(), "motivo de la consulta");
    }

    /**
     * Regla 6: la fecha y hora de la cita no puede ser anterior al momento actual.
     *
     * @param fechaHora fecha a validar.
     * @throws BadRequestException si la fecha ya paso.
     */
    private void validarFechaFutura(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("No se pueden registrar citas en fechas u horas anteriores a la actual.");
        }
    }

    /**
     * Convierte el estado a mayusculas y verifica que sea uno de los permitidos.
     *
     * @param estado estado recibido.
     * @return estado normalizado.
     * @throws BadRequestException si el estado no es valido.
     */
    private String normalizarEstado(String estado) {
        validador.validarTextoObligatorio(estado, "estado");
        String normalizado = estado.trim().toUpperCase();
        if (!EstadoCita.TODOS.contains(normalizado)) {
            throw new BadRequestException("El estado '" + estado + "' no es válido. Estados permitidos: "
                    + String.join(", ", EstadoCita.TODOS) + ".");
        }
        return normalizado;
    }

    /**
     * Reglas 4 y 5: revisa que ni el veterinario ni la mascota tengan otra cita
     * activa a menos de 15 minutos (antes o despues) de la fecha indicada.
     * Ejemplo: si hay una cita a las 10:00, se rechazan citas entre 9:46 y 10:14;
     * a las 9:45 o a las 10:15 si se permite.
     *
     * @param medico veterinario de la cita.
     * @param mascota mascota de la cita.
     * @param fechaHora fecha y hora de la cita.
     * @param citaExcluidaId id de la cita que se edita (null si es nueva).
     * @throws ConflictException si hay cruce de horario.
     */
    private void validarDisponibilidad(Medico medico, Mascota mascota, LocalDateTime fechaHora,
                                       Long citaExcluidaId) {
        LocalDateTime desde = fechaHora.minusMinutes(MINUTOS_SEPARACION);
        LocalDateTime hasta = fechaHora.plusMinutes(MINUTOS_SEPARACION);

        List<CitaMedica> crucesMedico = citaRepository.buscarCruceVeterinario(
                medico.getId(), desde, hasta, citaExcluidaId);
        if (!crucesMedico.isEmpty()) {
            throw new ConflictException("El veterinario " + medico.getNombres() + " " + medico.getApellidos()
                    + " no está disponible: ya tiene una cita el "
                    + crucesMedico.get(0).getFechaHora().format(FORMATO_FECHA)
                    + ". Debe haber al menos " + MINUTOS_SEPARACION + " minutos entre citas.");
        }

        List<CitaMedica> crucesMascota = citaRepository.buscarCruceMascota(
                mascota.getMascotaId(), desde, hasta, citaExcluidaId);
        if (!crucesMascota.isEmpty()) {
            throw new ConflictException("La mascota " + mascota.getNombreMascota()
                    + " ya tiene una cita el " + crucesMascota.get(0).getFechaHora().format(FORMATO_FECHA)
                    + ". No puede tener dos citas simultáneas.");
        }
    }
}
