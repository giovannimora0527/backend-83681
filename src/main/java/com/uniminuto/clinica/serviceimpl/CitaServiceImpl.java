package com.uniminuto.clinica.serviceimpl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String PROGRAMADA = "PROGRAMADA";
    private static final String CANCELADA = "CANCELADA";
    private static final String ATENDIDA = "ATENDIDA";
    private static final Set<String> ESTADOS = Set.of(
            "PENDIENTE", "CONFIRMADA", ATENDIDA, CANCELADA, PROGRAMADA);

    @Autowired private CitaRepository citaRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private MascotaRepository mascotaRepository;
    @Autowired private MedicoRepository medicoRepository;

    @Override
    public List<Cita> listarCitas() {
        List<Cita> citas = citaRepository.findAll();
        citas.sort(Comparator.comparing(Cita::getFechaHora,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return citas;
    }

    @Override
    public List<Cita> listarCitasRango(LocalDateTime inicio, LocalDateTime fin) {
        validarRango(inicio, fin);
        List<Cita> citas = citaRepository.findByFechaHoraBetween(inicio, fin);
        citas.sort(Comparator.comparing(Cita::getFechaHora));
        return citas;
    }

    @Override
    public MiRespuestaRS crearCita(CitaRq rq) {
        validarCita(rq, false);
        Cliente cliente = clienteRepository.findById(rq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente no existe."));
        Mascota mascota = mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota no existe."));
        Medico medico = medicoRepository.findById(rq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario no existe."));
        validarMascotaCliente(mascota, cliente);
        validarDisponibilidad(mascota, medico, rq.getFechaHora(), null);

        Cita cita = new Cita();
        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(rq.getFechaHora());
        cita.setMotivo(rq.getMotivo().trim());
        cita.setEstado(estado(rq.getEstado(), PROGRAMADA));
        citaRepository.save(cita);
        return respuesta("La cita se creó correctamente.");
    }

    @Override
    public MiRespuestaRS actualizarCita(CitaRq rq) {
        validarCita(rq, true);
        Cita cita = citaRepository.findById(rq.getId())
                .orElseThrow(() -> new BadRequestException("La cita no existe."));
        String estadoActual = cita.getEstado() == null ? "" : cita.getEstado().toUpperCase();
        if (CANCELADA.equals(estadoActual))
            throw new BadRequestException("Una cita cancelada no se puede actualizar.");
        if (ATENDIDA.equals(estadoActual))
            throw new BadRequestException("Una cita atendida no se puede modificar.");

        Cliente cliente = clienteRepository.findById(rq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente no existe."));
        Mascota mascota = mascotaRepository.findById(rq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota no existe."));
        Medico medico = medicoRepository.findById(rq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario no existe."));
        validarMascotaCliente(mascota, cliente);
        validarDisponibilidad(mascota, medico, rq.getFechaHora(), cita.getId());

        cita.setCliente(cliente);
        cita.setMascota(mascota);
        cita.setMedico(medico);
        cita.setFechaHora(rq.getFechaHora());
        cita.setMotivo(rq.getMotivo().trim());
        cita.setEstado(estado(rq.getEstado(), null));
        citaRepository.save(cita);
        return respuesta("La cita se actualizó correctamente.");
    }

    private void validarDisponibilidad(Mascota mascota, Medico medico,
                                       LocalDateTime fechaHora, Long citaId) {
        boolean veterinarioOcupado = citaRepository.findByMedicoAndFechaHoraBetween(
                        medico, fechaHora.minusMinutes(15), fechaHora.plusMinutes(15))
                .stream()
                .anyMatch(c -> !Objects.equals(c.getId(), citaId)
                        && !CANCELADA.equalsIgnoreCase(c.getEstado()));
        if (veterinarioOcupado)
            throw new BadRequestException("El veterinario ya tiene una cita dentro de los 15 minutos anteriores o posteriores.");

        boolean mascotaOcupada = citaRepository.findByMascotaAndFechaHora(mascota, fechaHora)
                .stream()
                .anyMatch(c -> !Objects.equals(c.getId(), citaId)
                        && !CANCELADA.equalsIgnoreCase(c.getEstado()));
        if (mascotaOcupada)
            throw new BadRequestException("La mascota ya tiene una cita en ese horario.");
    }

    private void validarMascotaCliente(Mascota mascota, Cliente cliente) {
        if (mascota.getCliente() == null
                || !Objects.equals(mascota.getCliente().getId(), cliente.getId()))
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente.");
    }

    private void validarCita(CitaRq rq, boolean actualizar) {
        if (rq == null) throw new BadRequestException("La cita no puede ser nula.");
        if (actualizar && rq.getId() == null)
            throw new BadRequestException("El identificador de la cita es obligatorio.");
        if (rq.getClienteId() == null)
            throw new BadRequestException("El cliente es obligatorio.");
        if (rq.getMascotaId() == null)
            throw new BadRequestException("La mascota es obligatoria.");
        if (rq.getMedicoId() == null)
            throw new BadRequestException("El veterinario es obligatorio.");
        if (rq.getFechaHora() == null)
            throw new BadRequestException("La fecha y hora son obligatorias.");
        if (rq.getFechaHora().isBefore(LocalDateTime.now()))
            throw new BadRequestException("No se pueden registrar citas en fechas anteriores a la fecha actual.");
        if (rq.getMotivo() == null || rq.getMotivo().isBlank() || rq.getMotivo().trim().length() > 1000)
            throw new BadRequestException("El motivo es obligatorio y no puede superar 1000 caracteres.");
        if (actualizar && (rq.getEstado() == null || rq.getEstado().isBlank()))
            throw new BadRequestException("El estado de la cita es obligatorio.");
        if (rq.getEstado() != null && !rq.getEstado().isBlank()
                && !ESTADOS.contains(rq.getEstado().trim().toUpperCase()))
            throw new BadRequestException("El estado de la cita no es válido.");
    }

    private String estado(String estado, String predeterminado) {
        return estado == null || estado.isBlank()
                ? predeterminado
                : estado.trim().toUpperCase();
    }

    private void validarRango(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null)
            throw new BadRequestException("Las fechas no pueden ser nulas.");
        if (inicio.isAfter(fin))
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha final.");
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }
}
