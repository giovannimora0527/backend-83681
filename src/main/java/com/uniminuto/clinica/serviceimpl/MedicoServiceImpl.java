package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoServiceImpl implements MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Override
    public List<Medico> listarMedicos() {
        return medicoRepository.findAll();
    }

    @Override
    public Medico buscarMedico(String tipoDocumento, String numeroDocumento) throws BadRequestException {
        if (numeroDocumento == null || numeroDocumento.isEmpty()) {
            throw new BadRequestException("El número de documento es obligatorio");
        }
        if (tipoDocumento == null || tipoDocumento.isEmpty()) {
            throw new BadRequestException("El tipo de documento es obligatorio");
        }

        Optional<Medico> medico = medicoRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento);
        if (medico.isEmpty()) {
            throw new BadRequestException("Medico no encontrado");
        }

        return medico.get();
    }

    @Override
    public Medico buscarMedicoPorRegistroProfesional(String registroProfesional) throws BadRequestException {
        if (registroProfesional == null || registroProfesional.isEmpty()) {
            throw new BadRequestException("El registro profesional es obligatorio");
        }

        Optional<Medico> medico = medicoRepository.findByRegistroProfesional(registroProfesional);
        if (medico.isEmpty()) {
            throw new BadRequestException("Medico no encontrado");
        }

        return medico.get();
    }

    @Override
    public MiRespuestaRS guardarMedico(MedicoRq rq) throws BadRequestException {
        this.validar(rq);
        Especializacion especializacion = this.obtenerEspecializacion(rq.getEspecializacionId());

        Medico medico = new Medico();
        medico.setTipoDocumento(rq.getTipoDocumento());
        medico.setNumeroDocumento(rq.getNumeroDocumento());
        medico.setNombres(rq.getNombres());
        medico.setApellidos(rq.getApellidos());
        medico.setTelefono(rq.getTelefono());
        medico.setRegistroProfesional(rq.getRegistroProfesional());
        medico.setEspecializacion(especializacion);

        medicoRepository.save(medico);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Médico guardado correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarMedico(MedicoRq rq) throws BadRequestException {
        this.validar(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio para actualizar");
        }

        Optional<Medico> optMedico = medicoRepository.findById(rq.getId());
        if (optMedico.isEmpty()) {
            throw new BadRequestException("El médico con ID " + rq.getId() + " no existe");
        }

        Especializacion especializacion = this.obtenerEspecializacion(rq.getEspecializacionId());

        Medico medico = optMedico.get();
        medico.setTipoDocumento(rq.getTipoDocumento());
        medico.setNumeroDocumento(rq.getNumeroDocumento());
        medico.setNombres(rq.getNombres());
        medico.setApellidos(rq.getApellidos());
        medico.setTelefono(rq.getTelefono());
        medico.setRegistroProfesional(rq.getRegistroProfesional());
        medico.setEspecializacion(especializacion);

        medicoRepository.save(medico);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Médico actualizado correctamente");
        return respuesta;
    }

    private void validar(MedicoRq rq) throws BadRequestException {
        if (rq == null || rq.getNumeroDocumento() == null || rq.getNumeroDocumento().trim().isEmpty()) {
            throw new BadRequestException("El número de documento es obligatorio");
        }
        if (rq.getEspecializacionId() == null) {
            throw new BadRequestException("La especialización es obligatoria");
        }
    }

    private Especializacion obtenerEspecializacion(Long id) throws BadRequestException {
        Optional<Especializacion> opt = especializacionRepository.findById(id);
        if (opt.isEmpty()) {
            throw new BadRequestException("La especialización con ID " + id + " no existe");
        }
        return opt.get();
    }
}
