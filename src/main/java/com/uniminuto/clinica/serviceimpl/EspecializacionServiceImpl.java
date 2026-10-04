package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Override
    public List<Especializacion> listar() {
        return especializacionRepository.findAll();
    }

    @Override
    public MiRespuestaRS guardar(EspecializacionRq rq) throws BadRequestException {
        this.validar(rq);

        Especializacion especializacion = new Especializacion();
        especializacion.setNombre(rq.getNombre());
        especializacion.setDescripcion(rq.getDescripcion());
        especializacion.setCodigoEspecializacion(rq.getCodigoEspecializacion());

        especializacionRepository.save(especializacion);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Especialización guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizar(EspecializacionRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El ID de la especialización es obligatorio para actualizar");
        }

        Optional<Especializacion> opt = especializacionRepository.findById(rq.getId());
        if (opt.isEmpty()) {
            throw new BadRequestException("La especialización con ID " + rq.getId() + " no existe");
        }

        Especializacion especializacion = opt.get();
        especializacion.setNombre(rq.getNombre());
        especializacion.setDescripcion(rq.getDescripcion());
        especializacion.setCodigoEspecializacion(rq.getCodigoEspecializacion());

        especializacionRepository.save(especializacion);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Especialización actualizada correctamente");
        return respuesta;
    }

    private void validar(EspecializacionRq rq) throws BadRequestException {
        if (rq == null || rq.getNombre() == null || rq.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la especialización es obligatorio");
        }
        if (rq.getCodigoEspecializacion() == null || rq.getCodigoEspecializacion().trim().isEmpty()) {
            throw new BadRequestException("El código de la especialización es obligatorio");
        }
    }
}
