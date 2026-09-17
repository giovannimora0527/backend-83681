package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Historia_Medica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.Historia_MedicaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.repository.Historia_MedicaRepository;
import com.uniminuto.clinica.service.Historia_MedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de historias médicas.
 */
@Service
public class HistoriaMedicaServiceImpl implements Historia_MedicaService {

    /** Repositorio para acceder a las historias en BD. */
    @Autowired
    private Historia_MedicaRepository historiaRepo;

    /**
     * Filtra historias por rango de fechas delegando en el repositorio.
     * Si inicio o fin son nulos, devuelve todas las historias ordenadas por fecha descendente.
     * @param inicio fecha inicial o null
     * @param fin fecha final o null
     * @return lista de historias
     */
    @Override
    public List<Historia_Medica> filtrarHistorias(LocalDateTime inicio, LocalDateTime fin) {
       if (inicio == null || fin == null) {
           return this.historiaRepo.findAllByOrderByFechaCreacionDesc();
       }
       return this.historiaRepo.findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(inicio, fin);
    }

    /**
     * Guarda una historia médica y asigna la fecha de creación automatica si no llega.
     *
     * @param historiaMedicaRq entidad con el paciente asociado
     * @return respuesta con el resultado
     */
    @Override
    public UsuarioRS guardarHistoria(Historia_MedicaRq historiaMedicaRq) throws BadRequestException {
       if (historiaMedicaRq == null) {
           throw new BadRequestException("La historia no puede ser nula");
       }
       if (historiaMedicaRq.getClienteId() == null) {
           throw new BadRequestException("clienteId es requerido");
       }

       Historia_Medica historia = new Historia_Medica();
       historia.setClienteId(historiaMedicaRq.getClienteId());
       historia.setFechaCreacion(historiaMedicaRq.getFechaCreacion() != null ? historiaMedicaRq.getFechaCreacion() : LocalDateTime.now());

       this.historiaRepo.save(historia);

       UsuarioRS rs = new UsuarioRS();
       rs.setStatus(200);
       rs.setMessage("Historia guardada correctamente");
       return rs;
    }

    /**
     * Actualiza una historia médica existente.
     *
     * @param historiaMedicaRq datos con el id y los cambios
     * @return respuesta con el resultado
     */
    @Override
    public UsuarioRS actualizarHistoria(Historia_MedicaRq historiaMedicaRq) throws BadRequestException {
       if (historiaMedicaRq == null || historiaMedicaRq.getId() == null) {
           throw new BadRequestException("El id de la historia es requerido para actualizar");
       }

       Optional<Historia_Medica> opt = this.historiaRepo.findById(historiaMedicaRq.getId());
       if (opt.isEmpty()) {
           throw new BadRequestException("La historia con ID " + historiaMedicaRq.getId() + " no existe");
       }

       Historia_Medica actual = opt.get();
       if (historiaMedicaRq.getClienteId() != null) {
           actual.setClienteId(historiaMedicaRq.getClienteId());
       }
       if (historiaMedicaRq.getFechaCreacion() != null) {
           actual.setFechaCreacion(historiaMedicaRq.getFechaCreacion());
       }

       this.historiaRepo.save(actual);

       UsuarioRS rs = new UsuarioRS();
       rs.setStatus(200);
       rs.setMessage("Historia actualizada correctamente");
       return rs;
    }
}
