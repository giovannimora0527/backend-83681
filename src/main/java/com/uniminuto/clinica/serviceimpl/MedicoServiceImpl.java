package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecializacionRepository especializacionRepository;
    private final CitaRepository citaRepository;

    public MedicoServiceImpl(
            MedicoRepository medicoRepository,
            EspecializacionRepository especializacionRepository,
            CitaRepository citaRepository) {
        this.medicoRepository = medicoRepository;
        this.especializacionRepository = especializacionRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    public List<Medico> listarMedicos() {
        return medicoRepository.findAll();
    }

    @Override
    @Transactional
    public MiRespuestaRS guardarMedico(MedicoRq rq) {
        validarMedico(rq);
        if (medicoRepository.existsByTipoDocumentoAndNumeroDocumento(
                rq.getTipoDocumento(), rq.getNumeroDocumento())) {
            throw new ConflictException("Ya existe un médico con ese tipo y número de documento.");
        }
        if (medicoRepository.existsByRegistroProfesional(rq.getRegistroProfesional())) {
            throw new ConflictException("Ya existe un médico con ese registro profesional.");
        }

        Especializacion especializacion = especializacionRepository.findById(rq.getEspecializacionId())
                .orElseThrow(() -> new NotFoundException("La especialización seleccionada no existe."));

        Medico medico = new Medico();
        medico.setTipoDocumento(rq.getTipoDocumento().trim());
        medico.setNumeroDocumento(rq.getNumeroDocumento().trim());
        medico.setNombres(rq.getNombres().trim());
        medico.setApellidos(rq.getApellidos().trim());
        medico.setTelefono(rq.getTelefono() == null || rq.getTelefono().isBlank() ? null : rq.getTelefono().trim());
        medico.setRegistroProfesional(rq.getRegistroProfesional().trim());
        medico.setEspecializacion(especializacion);
        medico = medicoRepository.save(medico);

        return new MiRespuestaRS(HttpStatus.CREATED.value(), "Médico creado correctamente.", medico.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarMedico(MedicoRq rq) {
        validarMedico(rq);
        if (rq.getId() == null) {
            throw new BadRequestException("El identificador del médico es obligatorio.");
        }

        Medico medico = medicoRepository.findById(rq.getId())
                .orElseThrow(() -> new NotFoundException("El médico que intenta actualizar no existe."));
        if (medicoRepository.existsByTipoDocumentoAndNumeroDocumentoAndIdNot(
                rq.getTipoDocumento(), rq.getNumeroDocumento(), rq.getId())) {
            throw new ConflictException("Ya existe otro médico con ese tipo y número de documento.");
        }
        if (medicoRepository.existsByRegistroProfesionalAndIdNot(rq.getRegistroProfesional(), rq.getId())) {
            throw new ConflictException("Ya existe otro médico con ese registro profesional.");
        }

        Especializacion especializacion = especializacionRepository.findById(rq.getEspecializacionId())
                .orElseThrow(() -> new NotFoundException("La especialización seleccionada no existe."));

        medico.setTipoDocumento(rq.getTipoDocumento().trim());
        medico.setNumeroDocumento(rq.getNumeroDocumento().trim());
        medico.setNombres(rq.getNombres().trim());
        medico.setApellidos(rq.getApellidos().trim());
        medico.setTelefono(rq.getTelefono() == null || rq.getTelefono().isBlank() ? null : rq.getTelefono().trim());
        medico.setRegistroProfesional(rq.getRegistroProfesional().trim());
        medico.setEspecializacion(especializacion);
        medicoRepository.save(medico);

        return new MiRespuestaRS(HttpStatus.OK.value(), "Médico actualizado correctamente.", medico.getId());
    }

    @Override
    @Transactional
    public MiRespuestaRS eliminarMedico(Long id) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El médico que intenta eliminar no existe."));
        if (citaRepository.existsByMedico_Id(id)) {
            throw new ConflictException("No se puede eliminar el médico porque tiene citas asociadas.");
        }

        medicoRepository.delete(medico);
        return new MiRespuestaRS(HttpStatus.OK.value(), "Médico eliminado correctamente.", id);
    }

    private void validarMedico(MedicoRq rq) {
        if (rq == null || esVacio(rq.getTipoDocumento()) || esVacio(rq.getNumeroDocumento())
                || esVacio(rq.getNombres()) || esVacio(rq.getApellidos())
                || esVacio(rq.getRegistroProfesional()) || rq.getEspecializacionId() == null) {
            throw new BadRequestException("Todos los campos obligatorios del médico deben estar completos.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
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
}
