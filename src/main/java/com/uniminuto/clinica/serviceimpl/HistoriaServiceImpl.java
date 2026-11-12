package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class HistoriaServiceImpl implements HistoriaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Override
    public List<HistoriaMedicaRs> getAllHistorias() {
        List<HistoriaMedica> historias = historiaMedicaRepository.findAll();
        List<HistoriaMedicaRs> resultados = new ArrayList<>();
        if (!historias.isEmpty()) {
            resultados.addAll(historias.stream().map(historia -> {
                HistoriaMedicaRs historiaMedicaRs = new HistoriaMedicaRs();
                historiaMedicaRs.setHistoria(historia);
                historiaMedicaRs.setAnotaciones(anotacionHistoriaRepository
                        .findByHistoria(historia).stream()
                        .map(AnotacionHistoriaRs::new).toList());
                return historiaMedicaRs;
            }).toList());
        }
        List<AnotacionHistoriaRs> anotaciones = resultados.stream()
                .flatMap(historia -> historia.getAnotaciones().stream()).toList();
        List<Long> citaIds = anotaciones.stream()
                .map(AnotacionHistoriaRs::getCita)
                .filter(Objects::nonNull)
                .map(cita -> cita.getId())
                .distinct().toList();
        if (!citaIds.isEmpty()) {
            Map<Long, List<FormulaMedica>> formulasPorCita = formulaMedicaRepository
                    .findByCita_IdInOrderByFechaCreacionRegistroDesc(citaIds).stream()
                    .collect(Collectors.groupingBy(formula -> formula.getCita().getId()));
            anotaciones.stream().filter(anotacion -> anotacion.getCita() != null)
                    .forEach(anotacion -> anotacion.setFormulasMedicas(
                            formulasPorCita.getOrDefault(anotacion.getCita().getId(), List.of())));
        }
        return resultados;
    }

    @Override
    public List<HistoriaMedicaRs> buscarHistorias(String termino) {
        String busqueda = termino == null ? "" : termino.trim().toLowerCase(Locale.ROOT);
        return getAllHistorias().stream()
                .filter(resultado -> {
                    HistoriaMedica historia = resultado.getHistoria();
                    return busqueda.isEmpty()
                            || String.valueOf(historia.getId()).contains(busqueda)
                            || contiene(historia.getDiagnostico(), busqueda)
                            || contiene(historia.getMotivoConsulta(), busqueda)
                            || (historia.getMascota() != null
                            && contiene(historia.getMascota().getNombreMascota(), busqueda))
                            || resultado.getAnotaciones().stream().anyMatch(anotacion ->
                            contiene(anotacion.getDiagnostico(), busqueda)
                                    || contiene(anotacion.getObservaciones(), busqueda));
                })
                .toList();
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    @Override
    public MiRespuestaRS guardarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        this.validarHistoria(historiaMedicaRq);
        Cita cita = this.existeCita(historiaMedicaRq);
        if (this.historiaMedicaRepository.existsByCita(cita)) {
            throw new BadRequestException("La cita ya tiene una historia clínica registrada.");
        }

        HistoriaMedica historiaNueva = new HistoriaMedica();
        this.mapearHistoria(historiaNueva, historiaMedicaRq, cita);
        this.historiaMedicaRepository.save(historiaNueva);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Historia clínica guardada correctamente.");
        return rta;
    }

    @Override
    public MiRespuestaRS actualizarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        if (historiaMedicaRq == null || historiaMedicaRq.getId() == null || historiaMedicaRq.getId() <= 0) {
            throw new BadRequestException("El id de la historia clínica es obligatorio y debe ser mayor a cero.");
        }
        this.validarHistoria(historiaMedicaRq);
        Cita cita = this.existeCita(historiaMedicaRq);
        Optional<HistoriaMedica> optionalHistoria = this.historiaMedicaRepository
                .findById(historiaMedicaRq.getId());
        if (optionalHistoria.isEmpty()) {
            throw new BadRequestException("La historia clínica con el id proporcionado no existe.");
        }
        if (this.historiaMedicaRepository.existsByCitaAndIdNot(cita, historiaMedicaRq.getId())) {
            throw new BadRequestException("La cita ya está asociada a otra historia clínica.");
        }

        HistoriaMedica historiaExistente = optionalHistoria.get();
        this.mapearHistoria(historiaExistente, historiaMedicaRq, cita);
        this.historiaMedicaRepository.save(historiaExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Historia clínica actualizada correctamente.");
        return rta;
    }

    private Cita existeCita(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        Optional<Cita> optionalCita = this.citaRepository.findById(historiaMedicaRq.getCitaId());
        if (optionalCita.isEmpty()) {
            throw new BadRequestException("La cita seleccionada no existe.");
        }
        Cita cita = optionalCita.get();
        if (cita.getMascota() == null || cita.getMedico() == null) {
            throw new BadRequestException("La cita debe tener una mascota y un veterinario asignados.");
        }
        return cita;
    }

    private void mapearHistoria(HistoriaMedica historia, HistoriaMedicaRq solicitud, Cita cita) {
        historia.setCita(cita);
        historia.setMascota(cita.getMascota());
        historia.setMedico(cita.getMedico());
        historia.setFechaCreacion(LocalDateTime.now());
        historia.setDiagnostico(solicitud.getDiagnostico().trim());
        historia.setMotivoConsulta(cita.getMotivo() == null || cita.getMotivo().isBlank()
                ? "Consulta veterinaria" : cita.getMotivo().trim());
        if (historia.getEstado() == null || historia.getEstado().isBlank()) {
            historia.setEstado("Abierta");
        }
    }

    private void validarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        if (historiaMedicaRq == null) {
            throw new BadRequestException("La información de la historia clínica es obligatoria.");
        }
        if (historiaMedicaRq.getCitaId() == null || historiaMedicaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita es obligatorio y debe ser mayor a cero.");
        }
        if (historiaMedicaRq.getDiagnostico() == null || historiaMedicaRq.getDiagnostico().isBlank()) {
            throw new BadRequestException("El diagnóstico de la historia clínica es obligatorio.");
        }
    }
}
