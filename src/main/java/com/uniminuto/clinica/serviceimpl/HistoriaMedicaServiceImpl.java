package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.models.HistoriaVistaRS;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.uniminuto.clinica.util.Especificaciones;
import com.uniminuto.clinica.util.Paginacion;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import com.uniminuto.clinica.util.FechaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de la lógica de negocio de historias clínicas.
 * Reglas: la historia nace de una cita atendida, la mascota pertenece al cliente,
 * la cita pertenece a esa mascota y solo tiene una historia.
 */
@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    @Autowired
    private HistoriaMedicaRepository historiaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public List<HistoriaMedica> listarHistorias() {
        return historiaRepository.findAllByOrderByFechaHoraDesc();
    }

    @Override
    public List<HistoriaMedica> filtrarHistorias(String fechaInicial, String fechaFinal) throws BadRequestException {
        LocalDateTime inicio = FechaUtil.parsearFecha(fechaInicial, false);
        LocalDateTime fin = FechaUtil.parsearFecha(fechaFinal, true);
        if (inicio.isAfter(fin)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return historiaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);
    }

    @Override
    public List<HistoriaMedica> listarHistoriasPorMascota(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El ID de la mascota no puede estar vacío ni ser menor o igual a cero");
        }
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new BadRequestException("La mascota con ID " + mascotaId + " no existe", HttpStatus.NOT_FOUND);
        }
        return historiaRepository.findByMascotaMascotaIdOrderByFechaHoraDesc(mascotaId);
    }

    @Override
    public HistoriaMedica consultarHistoria(Long id) throws BadRequestException {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID de la historia no puede estar vacío ni ser menor o igual a cero");
        }
        return historiaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La historia clínica con ID " + id + " no existe", HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarHistoria(HistoriaMedicaRq historiaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(historiaRq);

        // Paso 2. La cita debe existir, estar atendida y pertenecer a la misma mascota y cliente
        Cita cita = citaRepository.findById(historiaRq.getCitaId())
                .orElseThrow(() -> new BadRequestException("La cita con ID " + historiaRq.getCitaId() + " no existe", HttpStatus.NOT_FOUND));
        if (!Cita.ATENDIDA.equals(cita.getEstado())) {
            throw new BadRequestException("Solo se puede crear una historia clínica a partir de una cita atendida", HttpStatus.CONFLICT);
        }
        if (historiaRepository.findByCitaId(cita.getId()).isPresent()) {
            throw new BadRequestException("La cita con ID " + cita.getId() + " ya tiene una historia clínica", HttpStatus.CONFLICT);
        }

        // Paso 3. Verifico cliente, mascota y médico
        Cliente cliente = clienteRepository.findById(historiaRq.getClienteId())
                .orElseThrow(() -> new BadRequestException("El cliente con ID " + historiaRq.getClienteId() + " no existe", HttpStatus.NOT_FOUND));
        Mascota mascota = mascotaRepository.findById(historiaRq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota con ID " + historiaRq.getMascotaId() + " no existe", HttpStatus.NOT_FOUND));
        Medico medico = medicoRepository.findById(historiaRq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario con ID " + historiaRq.getMedicoId() + " no existe", HttpStatus.NOT_FOUND));
        if (!mascota.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("La mascota seleccionada no pertenece al cliente seleccionado", HttpStatus.CONFLICT);
        }
        if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId())
                || !cita.getCliente().getClienteId().equals(cliente.getClienteId())) {
            throw new BadRequestException("Los datos de cliente o mascota no coinciden con la cita", HttpStatus.CONFLICT);
        }

        // Paso 4. Creo la historia clínica
        HistoriaMedica historia = new HistoriaMedica();
        historia.setCliente(cliente);
        historia.setMascota(mascota);
        historia.setCita(cita);
        historia.setMedico(medico);
        historia.setFechaHora(historiaRq.getFechaHora() != null ? historiaRq.getFechaHora() : cita.getFechaHora());
        historia.setMotivo(historiaRq.getMotivo().trim());
        historia.setDiagnostico(historiaRq.getDiagnostico().trim());
        historia.setObservaciones(historiaRq.getObservaciones());
        this.historiaRepository.save(historia);

        // Paso 5. Retorno la respuesta
        return respuesta("Historia clínica guardada correctamente");
    }

    @Override
    public MiRespuestaRS actualizarHistoria(HistoriaMedicaRq historiaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        if (historiaRq == null || historiaRq.getId() == null || historiaRq.getId() <= 0) {
            throw new BadRequestException("El ID de la historia es obligatorio para actualizar");
        }
        if (historiaRq.getMotivo() == null || historiaRq.getMotivo().isBlank()
                || historiaRq.getDiagnostico() == null || historiaRq.getDiagnostico().isBlank()) {
            throw new BadRequestException("El motivo y el diagnóstico son obligatorios");
        }

        // Paso 2. Verifico que la historia exista
        HistoriaMedica historia = this.consultarHistoria(historiaRq.getId());

        // Paso 3. Cliente, mascota y cita no se pueden reasignar
        if (historiaRq.getCitaId() != null && !historiaRq.getCitaId().equals(historia.getCita().getId())
                || historiaRq.getMascotaId() != null && !historiaRq.getMascotaId().equals(historia.getMascota().getMascotaId())) {
            throw new BadRequestException("No se puede cambiar la cita o la mascota de una historia clínica", HttpStatus.CONFLICT);
        }

        // Paso 4. Actualizo motivo, diagnóstico y observaciones
        historia.setMotivo(historiaRq.getMotivo().trim());
        historia.setDiagnostico(historiaRq.getDiagnostico().trim());
        historia.setObservaciones(historiaRq.getObservaciones());
        this.historiaRepository.save(historia);

        // Paso 5. Retorno la respuesta
        return respuesta("Historia clínica actualizada correctamente");
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(HistoriaMedicaRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (rq.getCitaId() == null || rq.getCitaId() <= 0) {
            throw new BadRequestException("La cita asociada a la historia es obligatoria");
        }
        if (rq.getClienteId() == null || rq.getClienteId() <= 0) {
            throw new BadRequestException("El ID del cliente es obligatorio");
        }
        if (rq.getMascotaId() == null || rq.getMascotaId() <= 0) {
            throw new BadRequestException("El ID de la mascota es obligatorio");
        }
        if (rq.getMedicoId() == null || rq.getMedicoId() <= 0) {
            throw new BadRequestException("El ID del veterinario es obligatorio");
        }
        if (rq.getMotivo() == null || rq.getMotivo().isBlank()) {
            throw new BadRequestException("El motivo de consulta es obligatorio");
        }
        if (rq.getDiagnostico() == null || rq.getDiagnostico().isBlank()) {
            throw new BadRequestException("El diagnóstico es obligatorio");
        }
    }

    /** {@inheritDoc} */
    @Override
    public PaginaRS<HistoriaVistaRS> listarHistoriasPaginadas(Filtros.Historia filtros, int pagina, int tamanio,
                                                              String campo, String direccion) throws BadRequestException {
        Pageable pageable = Paginacion.crear(pagina, tamanio, campo, direccion,
                Set.of("fechaHora", "motivo", "diagnostico", "fechaCreacion"), "fechaHora");
        Page<HistoriaMedica> page = historiaRepository.findAll(Especificaciones.historia(filtros), pageable);
        return PaginaRS.desde(page, this::aVista);
    }

    private HistoriaVistaRS aVista(HistoriaMedica h) {
        Cita cita = h.getCita();
        return HistoriaVistaRS.builder()
                .id(h.getId())
                .clienteId(h.getCliente().getClienteId())
                .clienteNombre((h.getCliente().getNombres() + " " + h.getCliente().getApellidos()).trim())
                .mascotaId(h.getMascota().getMascotaId().longValue())
                .mascotaNombre(h.getMascota().getNombreMascota())
                .citaId(cita.getId())
                .estadoCita(cita.getEstado())
                .medicoId(h.getMedico().getId())
                .medicoNombre((h.getMedico().getNombres() + " " + h.getMedico().getApellidos()).trim())
                .fechaHora(h.getFechaHora())
                .motivo(h.getMotivo())
                .diagnostico(h.getDiagnostico())
                .observaciones(h.getObservaciones())
                .fechaCreacion(h.getFechaCreacion())
                .build();
    }
}
