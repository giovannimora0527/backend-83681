package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicamentoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.MedicamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<Medicamento> listar() {
        return medicamentoRepository.findAll();
    }

    @Override
    public MiRespuestaRS guardar(MedicamentoRq rq) throws BadRequestException {
        this.validar(rq);

        Medicamento medicamento = new Medicamento();
        medicamento.setNombre(rq.getNombre());
        medicamento.setPresentacion(rq.getPresentacion());
        medicamento.setDescripcion(rq.getDescripcion());

        medicamentoRepository.save(medicamento);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Medicamento guardado correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizar(MedicamentoRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El ID del medicamento es obligatorio para actualizar");
        }

        Optional<Medicamento> opt = medicamentoRepository.findById(rq.getId());
        if (opt.isEmpty()) {
            throw new BadRequestException("El medicamento con ID " + rq.getId() + " no existe");
        }

        Medicamento medicamento = opt.get();
        medicamento.setNombre(rq.getNombre());
        medicamento.setPresentacion(rq.getPresentacion());
        medicamento.setDescripcion(rq.getDescripcion());

        medicamentoRepository.save(medicamento);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Medicamento actualizado correctamente");
        return respuesta;
    }

    private void validar(MedicamentoRq rq) throws BadRequestException {
        if (rq == null || rq.getNombre() == null || rq.getNombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del medicamento es obligatorio");
        }
    }
}
