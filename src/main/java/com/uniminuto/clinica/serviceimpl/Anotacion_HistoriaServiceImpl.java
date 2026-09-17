package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Anotacion_Historia;
import com.uniminuto.clinica.entity.Historia_Medica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Anotacion_HistoriaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.repository.Anotacion_HistoriaRepository;
import com.uniminuto.clinica.repository.Historia_MedicaRepository;
import com.uniminuto.clinica.service.Anotacion_HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de anotaciones de historia médica.
 */
@Service
public class Anotacion_HistoriaServiceImpl implements Anotacion_HistoriaService {

    /** Repositorio para persistir y consultar anotaciones. */
    @Autowired
    private Anotacion_HistoriaRepository anotacionRepo;

    /** Repositorio para validar existencia de la historia asociada. */
    @Autowired
    private Historia_MedicaRepository historiaRepo;

    /**
     * Crea una nueva anotación ligada a una historia existente.
     * Valida que historiaId exista y completa la fecha con el timestamp actual.
     * @param rq petición con historiaId, medicoId y descripcion
     * @return UsuarioRS con resultado de la operación
     */
    @Override
    public UsuarioRS guardarAnotacion(Anotacion_HistoriaRq rq) {
        UsuarioRS rs = new UsuarioRS();
        if (rq.getHistoriaId() == null) {
            throw new BadRequestException("historiaId es requerido");
        }
        Optional<Historia_Medica> hOpt = this.historiaRepo.findById(rq.getHistoriaId());
        if (hOpt.isEmpty()) {
            throw new BadRequestException("Historia no encontrada");
        }
        Anotacion_Historia a = new Anotacion_Historia();
        a.setHistoria(hOpt.get());
        a.setMedicoId(rq.getMedicoId());
        a.setDescripcion(rq.getDescripcion());
        a.setFecha(LocalDateTime.now());
        this.anotacionRepo.save(a);
        rs.setStatus(200);
        rs.setMessage("Anotación creada");
        return rs;
    }

    /**
     * Actualiza una anotación existente. Solo actualiza campos permitidos por la tabla.
     * @param rq petición que contiene el id de la anotación y los campos a actualizar
     * @return UsuarioRS con resultado de la operación
     */
    @Override
    public UsuarioRS actualizarAnotacion(Anotacion_HistoriaRq rq) {
        UsuarioRS rs = new UsuarioRS();
        if (rq.getId() == null) {
            throw new BadRequestException("id es requerido para actualizar");
        }
        Optional<Anotacion_Historia> aOpt = this.anotacionRepo.findById(rq.getId());
        if (aOpt.isEmpty()) {
            throw new BadRequestException("Anotación no encontrada");
        }
        Anotacion_Historia a = aOpt.get();
        if (rq.getDescripcion() != null) a.setDescripcion(rq.getDescripcion());
        if (rq.getMedicoId() != null) a.setMedicoId(rq.getMedicoId());
        // la columna fecha representa el momento de la anotación; no se altera aquí
        this.anotacionRepo.save(a);
        rs.setStatus(200);
        rs.setMessage("Anotación actualizada");
        return rs;
    }

    /**
     * Lista las anotaciones de una historia ordenadas por fecha descendente.
     * @param historiaId identificador de la historia
     * @return lista de anotaciones
     */
    @Override
    public List<Anotacion_Historia> listarAnotacionesByHistoria(Long historiaId) {
        if (historiaId == null) {
            throw new BadRequestException("historiaId es requerido");
        }
        Optional<Historia_Medica> hOpt = this.historiaRepo.findById(historiaId);
        if (hOpt.isEmpty()) {
            throw new BadRequestException("Historia no encontrada");
        }
        return this.anotacionRepo.findByHistoriaOrderByFechaDesc(hOpt.get());
    }

    /**
     * Lista las anotaciones de una historia dentro de un rango de fechas.
     *
     * @param historiaId identificador de la historia
     * @param fechaInicial fecha inicial del rango
     * @param fechaFinal fecha final del rango
     * @return lista de anotaciones dentro del rango especificado
     */
    @Override
    public List<Anotacion_Historia> listarAnotacionesByHistoriaYFecha(Long historiaId, LocalDateTime fechaInicial, LocalDateTime fechaFinal) {
        if (historiaId == null) {
            throw new BadRequestException("historiaId es requerido");
        }
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("fechaInicial y fechaFinal son requeridas para filtrar");
        }
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }

        Optional<Historia_Medica> hOpt = this.historiaRepo.findById(historiaId);
        if (hOpt.isEmpty()) {
            throw new BadRequestException("Historia no encontrada");
        }

        return this.anotacionRepo.findByHistoriaAndFechaBetweenOrderByFechaDesc(hOpt.get(), fechaInicial, fechaFinal);
    }

    /**
     * Filtra las anotaciones de una historia a partir de fechas recibidas como texto.
     *
     * @param historiaId identificador de la historia
     * @param fechaInicial fecha inicial en texto
     * @param fechaFinal fecha final en texto
     * @return lista de anotaciones dentro del rango especificado
     * @throws BadRequestException si el formato es inválido o la historia no existe
     */
    @Override
    public List<Anotacion_Historia> filtrarAnotaciones(Long historiaId, String fechaInicial, String fechaFinal) throws BadRequestException {
        if (historiaId == null) {
            throw new BadRequestException("historiaId es requerido");
        }
        if (fechaInicial == null || fechaInicial.isBlank() || fechaFinal == null || fechaFinal.isBlank()) {
            throw new BadRequestException("fechaInicial y fechaFinal son requeridas para filtrar");
        }

        LocalDateTime fi = parseFecha(fechaInicial, false);
        LocalDateTime ff = parseFecha(fechaFinal, true);
        return listarAnotacionesByHistoriaYFecha(historiaId, fi, ff);
    }

    /**
     * Convierte una fecha recibida en texto a LocalDateTime.
     *
     * @param valor texto de la fecha
     * @param esFechaFinal indica si es fecha final
     * @return fecha convertida
     * @throws BadRequestException si el formato es inválido
     */
    private LocalDateTime parseFecha(String valor, boolean esFechaFinal) throws BadRequestException {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                .toFormatter();

        try {
            TemporalAccessor parsed = formatter.parseBest(valor, LocalDateTime::from, LocalDate::from);
            if (parsed instanceof LocalDateTime) {
                return (LocalDateTime) parsed;
            }
            LocalDate fecha = LocalDate.from(parsed);
            return esFechaFinal ? fecha.atTime(LocalTime.MAX) : fecha.atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Formato de fecha inválido. Use yyyy-MM-dd, yyyy-MM-dd HH:mm:ss o yyyy-MM-ddTHH:mm:ss");
        }
    }
}
