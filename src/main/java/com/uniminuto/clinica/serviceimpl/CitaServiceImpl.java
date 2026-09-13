package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.CitaRs;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public List<CitaRs> filtrarCitasPorFecha(LocalDate fechaInicio, LocalDate fechaFin) throws BadRequestException {

        // 1. Validaciones de fecha obligatorias
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha fin");
        }

        // 2. Ajustar los tiempos para cubrir el día completo
        // El inicio arranca a las 00:00:00
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        // El fin termina a las 23:59:59
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        // 3. Consultar a la base de datos usando el repositorio
        List<Cita> citas = citaRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);

        // 4. Transformar los parámetros de cita en DTO (Objeto de transferencia de datos)
        return citas.stream().map(this::aPublico).collect(Collectors.toList());
    }

    @Override
    public CitaRs crearCita(CitaRq citaRq) throws BadRequestException {
        // 1. Validar que los datos obligatorios se relacionen
        this.validarCita(citaRq, true);

        // 2. Cre uno nuevo objeto Cita y asigna los datos recibidos
        Cita cita = new Cita();
        cita.setClienteId(citaRq.getClienteId());
        cita.setMascotaId(citaRq.getMascotaId());
        cita.setMedicoId(citaRq.getMedicoId());
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setMotivo(citaRq.getMotivo().trim());

        // 3. Asignar el estado por defecto ("Programada") si en la petición no se incluye ninguno
        cita.setEstado(citaRq.getEstado() == null || citaRq.getEstado().trim().isEmpty()
                ? "Programada" : citaRq.getEstado().trim());

        // 4. Guardar el nuevo registro en la base de datos a través del repositorio
        citaRepository.save(cita);

        // 5. Transforma la entidad guardada en DTO (Objeto de transferencia de datos) y la retorna
        return aPublico(cita);
    }

    @Override
    public CitaRs actualizarCita(CitaRq citaRq) throws BadRequestException {
        // 1. Validar que el ID sea obligatorio para poder actualizar
        if (citaRq == null || citaRq.getId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio para actualizar");
        }

        // 2. Buscar una cita existente en la base de datos
        Optional<Cita> optCita = this.citaRepository.findById(citaRq.getId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("Cita no encontrada");
        }

        // 3. Muestra la cita encontrada y realiza las validaciones generales de estructura
        Cita cita = optCita.get();
        this.validarCita(citaRq, false);

        // 4. Reemplazar únicamente los campos que se relacionen como nuevos en la petición
        if (citaRq.getClienteId() != null) cita.setClienteId(citaRq.getClienteId());
        if (citaRq.getMascotaId() != null) cita.setMascotaId(citaRq.getMascotaId());
        if (citaRq.getMedicoId() != null) cita.setMedicoId(citaRq.getMedicoId());
        if (citaRq.getFechaHora() != null) cita.setFechaHora(citaRq.getFechaHora());

        if (citaRq.getEstado() != null && !citaRq.getEstado().trim().isEmpty()) {
            cita.setEstado(citaRq.getEstado().trim());
        }
        if (citaRq.getMotivo() != null && !citaRq.getMotivo().trim().isEmpty()) {
            cita.setMotivo(citaRq.getMotivo().trim());
        }

        // 5. Guarda los cambios en la base de datos
        citaRepository.save(cita);

        // 6. Transforma la entidad actualizada en DTO (Objeto de transferencia de datos) y la retorna
        return aPublico(cita);
    }

    private void validarCita(CitaRq citaRq, boolean esCreacion) throws BadRequestException {
        // 1. Verificar que no se vaya a crear una cita vacía o nula
        if (citaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        // 2. Si la operación es para crear una nueva cita, todos los campos base son estrictamente obligatorios
        if (esCreacion) {
            // 3. Validar que existan las referencias a las otras tablas (FK)
            if (citaRq.getClienteId() == null) {
                throw new BadRequestException("El ID del cliente es obligatorio");
            }
            if (citaRq.getMascotaId() == null) {
                throw new BadRequestException("El ID de la mascota es obligatorio");
            }
            if (citaRq.getMedicoId() == null) {
                throw new BadRequestException("El ID del médico es obligatorio");
            }

            // 4. Validar que se haya asignado una fecha y hora exacta para la cita
            if (citaRq.getFechaHora() == null) {
                throw new BadRequestException("La fecha y hora de la cita son obligatorias");
            }

            // 5. Validar que el motivo sea relacionado y no sea solo espacios en blanco
            if (citaRq.getMotivo() == null || citaRq.getMotivo().trim().isEmpty()) {
                throw new BadRequestException("El motivo de la cita es obligatorio");
            }
        }
    }

    /**
     * Convierte la entidad Cita a un DTO (Objeto de transferencia de datos) de CitaRS.
     */

    private CitaRs aPublico(Cita cita) {
        CitaRs rs = new CitaRs();
        rs.setId(cita.getId());
        rs.setClienteId(cita.getClienteId());
        rs.setMascotaId(cita.getMascotaId());
        rs.setMedicoId(cita.getMedicoId());
        rs.setFechaHora(cita.getFechaHora());
        rs.setEstado(cita.getEstado());
        rs.setMotivo(cita.getMotivo());
        return rs;
    }
}
