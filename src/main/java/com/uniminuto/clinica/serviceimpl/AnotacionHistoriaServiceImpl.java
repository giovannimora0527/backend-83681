package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación de los servicios para la entidad AnotacionHistoria.
 * Contiene la lógica de negocio, validaciones y transformaciones de datos
 * necesarias antes de interactuar con la base de datos.
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
    private AnotacionHistoriaRepository anotacionRepository;

    /**
     * Obtiene una lista de anotaciones médicas filtradas por un rango de fechas.
     *
     * @param fechaInicio Fecha inicial de la búsqueda.
     * @param fechaFin Fecha final de la búsqueda.
     * @return Lista de objetos AnotacionHistoriaRs con los datos encontrados.
     * @throws BadRequestException Si las fechas son nulas o inconsistentes.
     */
    @Override
    public List<AnotacionHistoriaRs> listarAnotaciones(LocalDate fechaInicio, LocalDate fechaFin) throws BadRequestException {
        // 1. Validaciones de integridad de las fechas
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha inicial no puede ser mayor a la final");
        }

        // 2. Ajuste de tiempos para abarcar los días completos (00:00:00 a 23:59:59)
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        // 3. Consulta al repositorio y transformación de la lista de Entidades a DTOs
        return anotacionRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin).stream()
                .map(this::aPublico)
                .collect(Collectors.toList());
    }

    /**
     * Crea y registra una nueva anotación en una historia médica.
     *
     * @param anotacionRq Objeto con los datos necesarios para la anotación.
     * @return DTO con la información de la anotación registrada.
     * @throws BadRequestException Si faltan datos obligatorios.
     */
    @Override
    public AnotacionHistoriaRs crearAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        // 1. Ejecutar validaciones obligatorias para creación
        this.validarAnotacion(anotacionRq, true);

        // 2. Mapear los datos del DTO a una nueva Entidad
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoriaId(anotacionRq.getHistoriaId());
        anotacion.setMedicoId(anotacionRq.getMedicoId());
        anotacion.setDescripcion(anotacionRq.getDescripcion().trim());
        anotacion.setFecha(LocalDateTime.now()); // Asignación automática de la fecha actual

        // 3. Persistir en la base de datos
        anotacionRepository.save(anotacion);

        // 4. Retornar el objeto transformado a DTO
        return aPublico(anotacion);
    }

    /**
     * Actualiza la información de una anotación médica existente.
     *
     * @param anotacionRq Objeto con los datos a modificar.
     * @return DTO con la información de la anotación actualizada.
     * @throws BadRequestException Si no se envía el ID o el registro no existe.
     */
    @Override
    public AnotacionHistoriaRs actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        // 1. Validar que la petición incluya el identificador único
        if (anotacionRq == null || anotacionRq.getId() == null) {
            throw new BadRequestException("El ID de la anotación es obligatorio para actualizar");
        }

        // 2. Buscar el registro en la base de datos
        Optional<AnotacionHistoria> optAnotacion = this.anotacionRepository.findById(anotacionRq.getId());
        if (optAnotacion.isEmpty()) {
            throw new BadRequestException("Anotación no encontrada");
        }

        // 3. Extraer la entidad y ejecutar validaciones de actualización
        AnotacionHistoria anotacion = optAnotacion.get();
        this.validarAnotacion(anotacionRq, false);

        // 4. Actualizar únicamente los campos que contienen nuevos valores
        if (anotacionRq.getDescripcion() != null && !anotacionRq.getDescripcion().trim().isEmpty()) {
            anotacion.setDescripcion(anotacionRq.getDescripcion().trim());
        }

        if (anotacionRq.getMedicoId() != null) {
            anotacion.setMedicoId(anotacionRq.getMedicoId());
        }

        // 5. Guardar los cambios y retornar el DTO actualizado
        anotacionRepository.save(anotacion);
        return aPublico(anotacion);
    }

    /**
     * Método centralizado para validar la estructura e integridad de los datos de entrada.
     *
     * @param anotacionRq Objeto de petición a evaluar.
     * @param esCreacion Indicador (true si es creación, false si es actualización).
     * @throws BadRequestException Si alguna regla de validación no se cumple.
     */
    private void validarAnotacion(AnotacionHistoriaRq anotacionRq, boolean esCreacion) throws BadRequestException {
        // 1. Prevenir errores de valores nulos en el objeto principal
        if (anotacionRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        // 2. Exigir las llaves foráneas únicamente cuando es un registro nuevo
        if (esCreacion && anotacionRq.getHistoriaId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio");
        }
        if (esCreacion && anotacionRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }

        // 3. Validar que la descripción no esté vacía si es creación o si se envía para actualización
        if (esCreacion || anotacionRq.getDescripcion() != null) {
            if (anotacionRq.getDescripcion() == null || anotacionRq.getDescripcion().trim().isEmpty()) {
                throw new BadRequestException("La descripción es obligatoria");
            }
        }
    }

    /**
     * Convierte una entidad AnotacionHistoria en un DTO AnotacionHistoriaRs para exponer los datos.
     *
     * @param anotacion Entidad con los datos extraídos de la base de datos.
     * @return DTO estructurado para ser enviado en la respuesta de la API.
     */
    private AnotacionHistoriaRs aPublico(AnotacionHistoria anotacion) {
        AnotacionHistoriaRs rs = new AnotacionHistoriaRs();
        rs.setId(anotacion.getId());
        rs.setHistoriaId(anotacion.getHistoriaId());
        rs.setMedicoId(anotacion.getMedicoId());
        rs.setFecha(anotacion.getFecha());
        rs.setDescripcion(anotacion.getDescripcion());
        return rs;
    }
}
