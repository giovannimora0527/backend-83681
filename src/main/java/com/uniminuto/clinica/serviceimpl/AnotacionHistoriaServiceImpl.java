package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.UsuarioRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de anotaciones de historia médica.
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /** Repositorio para persistir y consultar anotaciones. */
    @Autowired
    private AnotacionHistoriaRepository anotacionRepo;

    /** Repositorio para validar existencia de la historia asociada. */
    @Autowired
    private HistoriaMedicaRepository historiaRepo;

    /**
     * Crea una nueva anotación ligada a una historia existente.
     * Valida que historiaId exista y completa la fecha con el timestamp actual.
     * @param rq petición con historiaId, medicoId y descripcion
     * @return UsuarioRS con resultado de la operación
     */
    @Override
    public UsuarioRS guardarAnotacion(AnotacionHistoriaRq rq) {
        UsuarioRS rs = new UsuarioRS();
        if (rq.getHistoriaId() == null) {
            throw new BadRequestException("historiaId es requerido");
        }
        Optional<HistoriaMedica> hOpt = this.historiaRepo.findById(rq.getHistoriaId());
        if (hOpt.isEmpty()) {
            throw new BadRequestException("Historia no encontrada");
        }
        AnotacionHistoria a = new AnotacionHistoria();
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
    public UsuarioRS actualizarAnotacion(AnotacionHistoriaRq rq) {
        UsuarioRS rs = new UsuarioRS();
        if (rq.getId() == null) {
            throw new BadRequestException("id es requerido para actualizar");
        }
        Optional<AnotacionHistoria> aOpt = this.anotacionRepo.findById(rq.getId());
        if (aOpt.isEmpty()) {
            throw new BadRequestException("Anotación no encontrada");
        }
        AnotacionHistoria a = aOpt.get();
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
    public List<AnotacionHistoria> listarAnotacionesByHistoria(Long historiaId) {
        if (historiaId == null) {
            throw new BadRequestException("historiaId es requerido");
        }
        Optional<HistoriaMedica> hOpt = this.historiaRepo.findById(historiaId);
        if (hOpt.isEmpty()) {
            throw new BadRequestException("Historia no encontrada");
        }
        return this.anotacionRepo.findByHistoriaOrderByFechaDesc(hOpt.get());
    }
}
