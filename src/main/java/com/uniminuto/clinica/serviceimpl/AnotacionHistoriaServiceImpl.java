package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public List<AnotacionHistoriaRs> listarAnotaciones() {
        return anotacionHistoriaRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(AnotacionHistoria::getFecha,
                        java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())))
                .map(AnotacionHistoriaRs::new)
                .toList();
    }

    @Override
    public List<AnotacionHistoriaRs> buscarPorHistoria(Long historiaId) {
        if (historiaId == null || historiaId <= 0) {
            throw new BadRequestException("El id de la historia debe ser mayor a cero.");
        }
        HistoriaMedica historia = historiaMedicaRepository.findById(historiaId)
                .orElseThrow(() -> new BadRequestException("La historia clínica no existe."));
        return anotacionHistoriaRepository.findByHistoria(historia).stream()
                .map(AnotacionHistoriaRs::new)
                .toList();
    }

    @Override
    public MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) {
        this.validarAnotacionHistoria(anotacionHistoriaRq);
        Optional<Cita> optCita = this.citaRepository.findById(anotacionHistoriaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + anotacionHistoriaRq.getCitaId()  +
                    " no existe. Por favor consulte al administrador del sistema o registre la cita antes de " +
                    "crear la anotación de historia clínica.");
        }

        HistoriaMedica historia = new HistoriaMedica();
        Cita cita = optCita.get();
        Optional<HistoriaMedica> historiaOpt = anotacionHistoriaRq.getHistoriaId() == null
                ? Optional.empty() : this.historiaMedicaRepository.findById(anotacionHistoriaRq.getHistoriaId());
        if (historiaOpt.isEmpty()) {
            throw new BadRequestException("La historia clínica seleccionada no existe.");
        }
        historia = historiaOpt.get();
        asociarCitaAHistoria(historia, cita, anotacionHistoriaRq.getMedicoId(),
                anotacionHistoriaRq.getDiagnostico());

        AnotacionHistoria nuevaAnotacion = new AnotacionHistoria();
        nuevaAnotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        nuevaAnotacion.setHistoria(historia);
        nuevaAnotacion.setDiagnostico(anotacionHistoriaRq.getDiagnostico());
        nuevaAnotacion.setObservaciones(anotacionHistoriaRq.getObservaciones());
        nuevaAnotacion.setMedico(cita.getMedico());
        nuevaAnotacion.setFecha(LocalDateTime.now());
        this.anotacionHistoriaRepository.save(nuevaAnotacion);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Anotación de historia clínica creada exitosamente");
        return rta;
    }

    @Override
    public MiRespuestaRS actualizarAnotacionHistoria(AnotacionHistoriaRq solicitud) {
        this.validarAnotacionHistoria(solicitud);
        if (solicitud.getId() == null || solicitud.getId() <= 0) {
            throw new BadRequestException("El id de la anotación es obligatorio y debe ser mayor a cero.");
        }
        AnotacionHistoria anotacion = anotacionHistoriaRepository.findById(solicitud.getId())
                .orElseThrow(() -> new BadRequestException("La anotación de historia clínica no existe."));
        HistoriaMedica historia = historiaMedicaRepository.findById(solicitud.getHistoriaId())
                .orElseThrow(() -> new BadRequestException("La historia clínica seleccionada no existe."));
        Cita cita = citaRepository.findById(solicitud.getCitaId())
                .orElseThrow(() -> new BadRequestException("La cita seleccionada no existe."));
        asociarCitaAHistoria(historia, cita, solicitud.getMedicoId(), solicitud.getDiagnostico());

        anotacion.setHistoria(historia);
        anotacion.setMedico(historia.getMedico());
        anotacion.setDescripcion(solicitud.getDescripcion().trim());
        anotacion.setDiagnostico(solicitud.getDiagnostico().trim());
        anotacion.setObservaciones(solicitud.getObservaciones().trim());
        anotacionHistoriaRepository.save(anotacion);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Anotación de historia clínica actualizada correctamente.");
        return respuesta;
    }

    private void asociarCitaAHistoria(HistoriaMedica historia, Cita cita, Long medicoId, String diagnostico) {
        if (historia.getMascota() == null || cita.getMascota() == null
                || !historia.getMascota().getMascotaId().equals(cita.getMascota().getMascotaId())) {
            throw new BadRequestException("La cita debe pertenecer a la mascota de la historia clínica.");
        }
        if (cita.getMedico() == null || !cita.getMedico().getId().equals(medicoId)) {
            throw new BadRequestException("El veterinario debe coincidir con el asignado a la cita.");
        }
        if (historia.getCita() != null && !historia.getCita().getId().equals(cita.getId())) {
            throw new BadRequestException("La cita debe ser la misma que está asociada a la historia clínica.");
        }
        if (historia.getMedico() != null && !historia.getMedico().getId().equals(cita.getMedico().getId())) {
            throw new BadRequestException("El veterinario debe coincidir con el asignado a la historia.");
        }

        historia.setCita(cita);
        historia.setMedico(cita.getMedico());
        if (historia.getDiagnostico() == null || historia.getDiagnostico().isBlank()) {
            historia.setDiagnostico(diagnostico.trim());
        }
        if (historia.getMotivoConsulta() == null || historia.getMotivoConsulta().isBlank()) {
            historia.setMotivoConsulta(cita.getMotivo() == null || cita.getMotivo().isBlank()
                    ? "Consulta veterinaria" : cita.getMotivo().trim());
        }
        if (historia.getEstado() == null || historia.getEstado().isBlank()) {
            historia.setEstado("Abierta");
        }
        historiaMedicaRepository.save(historia);
    }

    private void validarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("La anotación de historia clínica es obligatoria");
        }
        if (anotacionHistoriaRq.getCitaId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio");
        }
        if (anotacionHistoriaRq.getHistoriaId() == null) {
            throw new BadRequestException("El ID de la historia clínica es obligatorio");
        }
        if (anotacionHistoriaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (anotacionHistoriaRq.getDescripcion() == null || anotacionHistoriaRq.getDescripcion().isEmpty()) {
            throw new BadRequestException("La descripción de la anotación es obligatoria");
        }
        if (anotacionHistoriaRq.getDiagnostico() == null || anotacionHistoriaRq.getDiagnostico().isEmpty()) {
            throw new BadRequestException("El diagnóstico de la anotación es obligatorio");
        }
        if (anotacionHistoriaRq.getObservaciones() == null || anotacionHistoriaRq.getObservaciones().isEmpty()) {
            throw new BadRequestException("Las observaciones de la anotación son obligatorias");
        }
    }
}
