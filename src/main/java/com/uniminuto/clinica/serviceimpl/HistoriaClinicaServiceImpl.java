package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.HistoriaClinicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.HistoriaClinicaService;
import com.uniminuto.clinica.util.EstadoCita;
import com.uniminuto.clinica.util.RespuestaUtil;
import com.uniminuto.clinica.util.ValidadorEntidades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio de las historias clinicas.
 *
 * Reglas implementadas:
 * - El cliente y la mascota deben existir y la mascota debe ser del cliente.
 * - La cita debe existir, ser de esa mascota y estar ATENDIDA.
 * - Una cita solo puede tener UNA historia clinica (evita duplicados).
 * - El veterinario debe existir.
 * - La fecha de la consulta no puede ser anterior a la cita ni futura.
 * - Una historia CERRADA no admite nuevas anotaciones.
 */
@Service
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {

    /**
     * Estado de una historia que todavia acepta anotaciones.
     */
    public static final String ESTADO_ABIERTA = "ABIERTA";

    /**
     * Estado de una historia que ya no acepta anotaciones.
     */
    public static final String ESTADO_CERRADA = "CERRADA";

    /**
     * Repositorio de historias clinicas.
     */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Validaciones comunes (cliente, mascota, veterinario, cita).
     */
    @Autowired
    private ValidadorEntidades validador;

    @Override
    public List<HistoriaMedica> listarHistorias() {
        return historiaMedicaRepository.findAllByOrderByFechaConsultaDesc();
    }

    @Override
    public HistoriaMedica consultarHistoria(Long historiaId) throws BadRequestException {
        validador.validarIdObligatorio(historiaId, "historia clínica");
        return historiaMedicaRepository.findById(historiaId)
                .orElseThrow(() -> new NotFoundException("La historia clínica con ID " + historiaId + " no existe."));
    }

    @Override
    public MiRespuestaRS crearHistoria(HistoriaClinicaRq historiaRq) throws BadRequestException {
        // Paso 1. Campos obligatorios
        if (historiaRq == null) {
            throw new BadRequestException("Los datos de la historia clínica son obligatorios.");
        }
        validador.validarTextoObligatorio(historiaRq.getMotivo(), "motivo de consulta");
        validador.validarTextoObligatorio(historiaRq.getDiagnostico(), "diagnóstico");

        // Paso 2. Cliente y mascota existen y la mascota es del cliente
        Mascota mascota = validador.obtenerMascotaDelCliente(historiaRq.getClienteId(), historiaRq.getMascotaId());

        // Paso 3. La cita existe y es de esa mascota
        CitaMedica cita = validador.obtenerCita(historiaRq.getCitaId());
        validador.validarCitaDeLaMascota(cita, mascota);

        // Paso 4. Solo una cita ATENDIDA genera historia clinica
        if (!EstadoCita.ATENDIDA.equals(cita.getEstado())) {
            throw new ConflictException("Solo se puede crear la historia clínica de una cita ATENDIDA. "
                    + "Estado actual de la cita: " + cita.getEstado() + ".");
        }

        // Paso 5. Evitar duplicados: una historia por cita
        if (historiaMedicaRepository.existsByCitaId(cita.getId())) {
            throw new ConflictException("La cita seleccionada ya tiene una historia clínica registrada.");
        }

        // Paso 6. Veterinario existe
        Medico medico = validador.obtenerMedico(historiaRq.getMedicoId());

        // Paso 7. Fecha de la consulta: por defecto ahora; no antes de la cita ni en el futuro
        LocalDateTime fechaConsulta = historiaRq.getFechaHora() != null
                ? historiaRq.getFechaHora() : LocalDateTime.now();
        if (fechaConsulta.isBefore(cita.getFechaHora())) {
            throw new BadRequestException("La fecha de la consulta no puede ser anterior a la fecha de la cita.");
        }
        if (fechaConsulta.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de la consulta no puede ser una fecha futura.");
        }

        // Paso 8. Guardar
        HistoriaMedica historia = new HistoriaMedica();
        historia.setMascota(mascota);
        historia.setCita(cita);
        historia.setMedico(medico);
        historia.setFechaConsulta(fechaConsulta);
        historia.setMotivo(historiaRq.getMotivo().trim());
        historia.setDiagnostico(historiaRq.getDiagnostico().trim());
        historia.setObservaciones(historiaRq.getObservaciones());
        historia.setEstado(ESTADO_ABIERTA);
        historiaMedicaRepository.save(historia);

        return RespuestaUtil.crear(201, "Historia clínica creada correctamente para "
                + mascota.getNombreMascota() + ".");
    }

    @Override
    public MiRespuestaRS cerrarHistoria(Long historiaId) throws BadRequestException {
        HistoriaMedica historia = this.consultarHistoria(historiaId);
        if (ESTADO_CERRADA.equals(historia.getEstado())) {
            throw new ConflictException("La historia clínica ya se encuentra cerrada.");
        }
        historia.setEstado(ESTADO_CERRADA);
        historiaMedicaRepository.save(historia);
        return RespuestaUtil.crear(200, "Historia clínica cerrada correctamente.");
    }
}
