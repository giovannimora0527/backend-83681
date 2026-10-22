package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.util.EstadoCita;
import com.uniminuto.clinica.models.CitaVistaRS;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.uniminuto.clinica.util.Especificaciones;
import com.uniminuto.clinica.util.Paginacion;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import com.uniminuto.clinica.util.FechaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de la lógica de negocio de citas.
 * Reglas: entidades existentes, mascota del cliente, choques de horario de 15 minutos,
 * sin fechas pasadas, estados válidos y citas canceladas/atendidas no editables.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /** Margen en minutos antes y después de una cita para detectar choques. */
    private static final long MARGEN_MINUTOS = 15L;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAllByOrderByFechaHoraDesc();
    }

    @Override
    public List<Cita> filtrarCitas(String fechaInicial, String fechaFinal) throws BadRequestException {
        // Paso 1. Convierto y valido el rango
        LocalDateTime inicio = FechaUtil.parsearFecha(fechaInicial, false);
        LocalDateTime fin = FechaUtil.parsearFecha(fechaFinal, true);
        if (inicio.isAfter(fin)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }

        // Paso 2. Consulto las citas del rango
        return citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);
    }

    @Override
    public Cita consultarCita(Long id) throws BadRequestException {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID de la cita no puede estar vacío ni ser menor o igual a cero");
        }
        return citaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La cita con ID " + id + " no existe", HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada y normalizo el estado
        this.validarObjetoEntrada(citaRq);
        String estado = this.normalizarEstado(citaRq.getEstado());
        if (Cita.ATENDIDA.equals(estado) || Cita.CANCELADA.equals(estado)) {
            throw new BadRequestException("Una cita nueva solo puede registrarse como Pendiente, Confirmada o Programada");
        }
        this.validarFechaNoPasada(citaRq.getFechaHora());

        // Paso 2. Verifico cliente, mascota (del cliente) y médico
        Cliente cliente = this.obtenerCliente(citaRq.getClienteId());
        Mascota mascota = this.obtenerMascotaDelCliente(citaRq.getMascotaId(), citaRq.getClienteId());
        Medico medico = this.obtenerMedico(citaRq.getMedicoId());

        // Paso 3. Verifico que no haya choques de horario
        this.validarDisponibilidad(medico.getId(), mascota.getMascotaId(), citaRq.getFechaHora(), null);

        // Paso 4. Creo la cita
        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(estado);
        cita.setMotivo(citaRq.getMotivo().trim());
        this.citaRepository.save(cita);

        // Paso 5. Retorno la respuesta
        return respuesta("Cita guardada correctamente");
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(citaRq);
        if (citaRq.getId() == null || citaRq.getId() <= 0) {
            throw new BadRequestException("El ID de la cita no puede estar vacío ni ser menor o igual a cero");
        }

        // Paso 2. Verifico que la cita exista y que su estado permita modificarla
        Cita cita = this.consultarCita(citaRq.getId());
        if (Cita.CANCELADA.equals(cita.getEstado())) {
            throw new BadRequestException("La cita está cancelada y no puede modificarse. Use la operación de reactivar si corresponde", HttpStatus.CONFLICT);
        }
        if (Cita.ATENDIDA.equals(cita.getEstado())) {
            throw new BadRequestException("La cita ya fue atendida y su información histórica no puede modificarse", HttpStatus.CONFLICT);
        }

        // Paso 3. Valido los nuevos datos
        String estado = this.normalizarEstado(citaRq.getEstado());
        this.validarFechaNoPasada(citaRq.getFechaHora());
        Cliente cliente = this.obtenerCliente(citaRq.getClienteId());
        Mascota mascota = this.obtenerMascotaDelCliente(citaRq.getMascotaId(), citaRq.getClienteId());
        Medico medico = this.obtenerMedico(citaRq.getMedicoId());
        this.validarDisponibilidad(medico.getId(), mascota.getMascotaId(), citaRq.getFechaHora(), cita.getId());

        // Paso 4. Actualizo la cita
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(estado);
        cita.setMotivo(citaRq.getMotivo().trim());
        this.citaRepository.save(cita);

        // Paso 5. Retorno la respuesta
        return respuesta("Cita actualizada correctamente");
    }

    @Override
    public MiRespuestaRS reactivarCita(Long id) throws BadRequestException {
        // Paso 1. Verifico que la cita exista y esté cancelada
        Cita cita = this.consultarCita(id);
        if (!Cita.CANCELADA.equals(cita.getEstado())) {
            throw new BadRequestException("Solo se pueden reactivar citas canceladas", HttpStatus.CONFLICT);
        }

        // Paso 2. La cita no puede quedar en el pasado ni en choque de horario
        this.validarFechaNoPasada(cita.getFechaHora());
        this.validarDisponibilidad(cita.getMedico().getId(), cita.getMascota().getMascotaId(), cita.getFechaHora(), cita.getId());

        // Paso 3. Vuelvo la cita a Pendiente
        cita.setEstado(Cita.PENDIENTE);
        this.citaRepository.save(cita);

        // Paso 4. Retorno la respuesta
        return respuesta("Cita reactivada correctamente");
    }

    /**
     * Verifica que el veterinario y la mascota no tengan otra cita activa dentro del margen de 15 minutos.
     */
    private void validarDisponibilidad(Long medicoId, Integer mascotaId, LocalDateTime fechaHora, Long idActual) {
        LocalDateTime inicio = fechaHora.minusMinutes(MARGEN_MINUTOS);
        LocalDateTime fin = fechaHora.plusMinutes(MARGEN_MINUTOS);

        boolean medicoOcupado = citaRepository.findByMedicoIdAndFechaHoraBetween(medicoId, inicio, fin).stream()
                .anyMatch(c -> esActiva(c, idActual));
        if (medicoOcupado) {
            throw new BadRequestException("El veterinario ya tiene una cita en ese horario (margen de 15 minutos)", HttpStatus.CONFLICT);
        }

        boolean mascotaOcupada = citaRepository.findByMascotaMascotaIdAndFechaHoraBetween(mascotaId, inicio, fin).stream()
                .anyMatch(c -> esActiva(c, idActual));
        if (mascotaOcupada) {
            throw new BadRequestException("La mascota ya tiene una cita en ese horario (margen de 15 minutos)", HttpStatus.CONFLICT);
        }
    }

    private boolean esActiva(Cita cita, Long idActual) {
        boolean esOtra = idActual == null || !cita.getId().equals(idActual);
        return esOtra && !Cita.CANCELADA.equals(cita.getEstado());
    }

    private void validarFechaNoPasada(LocalDateTime fechaHora) {
        if (fechaHora.toLocalDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se pueden registrar citas en fechas anteriores a la fecha actual");
        }
    }

    private String normalizarEstado(String estado) {
        for (String permitido : Cita.ESTADOS) {
            if (permitido.equalsIgnoreCase(estado.trim())) {
                return permitido;
            }
        }
        throw new BadRequestException("Estado de cita inválido. Valores permitidos: " + String.join(", ", Cita.ESTADOS));
    }

    private Cliente obtenerCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new BadRequestException("El cliente con ID " + clienteId + " no existe", HttpStatus.NOT_FOUND));
    }

    private Medico obtenerMedico(Long medicoId) {
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new BadRequestException("El veterinario con ID " + medicoId + " no existe", HttpStatus.NOT_FOUND));
    }

    private Mascota obtenerMascotaDelCliente(Integer mascotaId, Long clienteId) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new BadRequestException("La mascota con ID " + mascotaId + " no existe", HttpStatus.NOT_FOUND));
        if (mascotaRepository.findByMascotaIdAndClienteClienteId(mascotaId, clienteId).isEmpty()) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente seleccionado", HttpStatus.CONFLICT);
        }
        return mascota;
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(CitaRq citaRq) throws BadRequestException {
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (citaRq.getClienteId() == null || citaRq.getClienteId() <= 0) {
            throw new BadRequestException("El ID del cliente no puede estar vacío ni ser menor o igual a cero");
        }
        if (citaRq.getMascotaId() == null || citaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El ID de la mascota no puede estar vacío ni ser menor o igual a cero");
        }
        if (citaRq.getMedicoId() == null || citaRq.getMedicoId() <= 0) {
            throw new BadRequestException("El ID del veterinario no puede estar vacío ni ser menor o igual a cero");
        }
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException("La fecha y hora de la cita son obligatorias");
        }
        if (citaRq.getMotivo() == null || citaRq.getMotivo().isBlank()) {
            throw new BadRequestException("El motivo de la consulta es obligatorio");
        }
        if (citaRq.getEstado() == null || citaRq.getEstado().isBlank()) {
            throw new BadRequestException("El estado de la cita es obligatorio");
        }
    }

    /** {@inheritDoc} */
    @Override
    public PaginaRS<CitaVistaRS> listarCitasPaginadas(Filtros.Cita filtros, int pagina, int tamanio,
                                                      String campo, String direccion) throws BadRequestException {
        // Paso 1. Normalizo el estado del filtro (acepta mayúsculas o minúsculas)
        Filtros.Cita f = filtros == null ? null : new Filtros.Cita(filtros.clienteId(), filtros.mascotaId(),
                filtros.medicoId(),
                (filtros.estado() == null || filtros.estado().isBlank()) ? null : EstadoCita.desde(filtros.estado()).getEtiqueta(),
                filtros.fechaDesde(), filtros.fechaHasta(), filtros.texto());

        // Paso 2. Valido la paginación y consulto la página
        Pageable pageable = Paginacion.crear(pagina, tamanio, campo, direccion,
                Set.of("fechaHora", "estado", "motivo", "clienteId", "mascotaId", "medicoId"), "fechaHora");
        Page<Cita> page = citaRepository.findAll(Especificaciones.cita(f), pageable);

        // Paso 3. Convierto cada cita en su vista resumida
        return PaginaRS.desde(page, this::aVista);
    }

    private CitaVistaRS aVista(Cita c) {
        return CitaVistaRS.builder()
                .id(c.getId())
                .clienteId(c.getCliente().getClienteId())
                .clienteNombre((c.getCliente().getNombres() + " " + c.getCliente().getApellidos()).trim())
                .mascotaId(c.getMascota().getMascotaId().longValue())
                .mascotaNombre(c.getMascota().getNombreMascota())
                .medicoId(c.getMedico().getId())
                .medicoNombre((c.getMedico().getNombres() + " " + c.getMedico().getApellidos()).trim())
                .fechaHora(c.getFechaHora())
                .motivo(c.getMotivo())
                .estado(c.getEstado())
                .build();
    }
}
