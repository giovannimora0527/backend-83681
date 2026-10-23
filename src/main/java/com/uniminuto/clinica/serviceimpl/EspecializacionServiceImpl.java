package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    private final EspecializacionRepository especializacionRepository;
    private final MedicoRepository medicoRepository;

    public EspecializacionServiceImpl(
            EspecializacionRepository especializacionRepository,
            MedicoRepository medicoRepository) {
        this.especializacionRepository = especializacionRepository;
        this.medicoRepository = medicoRepository;
    }

    @Override
    public List<Especializacion> listarEspecializaciones() {
        return especializacionRepository.findAll();
    }

    @Override
    @Transactional
    public MiRespuestaRS crearEspecializacion(EspecializacionRq rq) {
        validar(rq);
        validarDuplicados(rq, null);
        Especializacion especializacion = new Especializacion();
        aplicarCambios(especializacion, rq);
        especializacion = especializacionRepository.save(especializacion);
        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Especialización creada correctamente.",
                especializacion.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarEspecializacion(EspecializacionRq rq) {
        validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El identificador de la especialización es obligatorio.");
        }
        Especializacion especializacion = especializacionRepository.findById(rq.getId())
                .orElseThrow(() -> new NotFoundException("La especialización seleccionada no existe."));
        validarDuplicados(rq, rq.getId());
        aplicarCambios(especializacion, rq);
        especializacionRepository.save(especializacion);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Especialización actualizada correctamente.",
                especializacion.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS eliminarEspecializacion(Long id) {
        Especializacion especializacion = especializacionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("La especialización seleccionada no existe."));
        if (medicoRepository.existsByEspecializacion_Id(id)) {
            throw new ConflictException("No se puede eliminar la especialización porque tiene médicos asociados.");
        }
        especializacionRepository.delete(especializacion);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Especialización eliminada correctamente.", id);
    }

    private void validar(EspecializacionRq rq) {
        if (rq == null || vacio(rq.getNombre()) || vacio(rq.getCodigoEspecializacion())) {
            throw new BadRequestException("El código y el nombre de la especialización son obligatorios.");
        }
        if (rq.getNombre().trim().length() > 100 || rq.getCodigoEspecializacion().trim().length() > 10) {
            throw new BadRequestException("El nombre admite hasta 100 caracteres y el código hasta 10.");
        }
    }

    private void validarDuplicados(EspecializacionRq rq, Long id) {
        String codigo = rq.getCodigoEspecializacion().trim();
        String nombre = rq.getNombre().trim();
        boolean codigoDuplicado = id == null
                ? especializacionRepository.existsByCodigoEspecializacionIgnoreCase(codigo)
                : especializacionRepository.existsByCodigoEspecializacionIgnoreCaseAndIdNot(codigo, id);
        boolean nombreDuplicado = id == null
                ? especializacionRepository.existsByNombreIgnoreCase(nombre)
                : especializacionRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id);
        if (codigoDuplicado || nombreDuplicado) {
            throw new ConflictException("Ya existe una especialización con ese código o nombre.");
        }
    }

    private void aplicarCambios(Especializacion especializacion, EspecializacionRq rq) {
        especializacion.setCodigoEspecializacion(rq.getCodigoEspecializacion().trim().toUpperCase());
        especializacion.setNombre(rq.getNombre().trim());
        especializacion.setDescripcion(rq.getDescripcion() == null || rq.getDescripcion().isBlank()
                ? null : rq.getDescripcion().trim());
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
