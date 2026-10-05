package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
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
    private  MedicoRepository medicoRepository;

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
    public Medico guardarMedico(MedicoRq solicitud) throws BadRequestException {
        return guardar(solicitud, false);
    }

    @Override
    public Medico actualizarMedico(MedicoRq solicitud) throws BadRequestException {
        return guardar(solicitud, true);
    }

    private Medico guardar(MedicoRq solicitud, boolean actualizar) {
        if (solicitud == null || solicitud.getTipoDocumento() == null || solicitud.getTipoDocumento().isBlank()
                || solicitud.getNumeroDocumento() == null || solicitud.getNumeroDocumento().isBlank()
                || solicitud.getNombres() == null || solicitud.getNombres().isBlank()
                || solicitud.getApellidos() == null || solicitud.getApellidos().isBlank()
                || solicitud.getRegistroProfesional() == null || solicitud.getRegistroProfesional().isBlank()
                || solicitud.getEspecializacionId() == null) {
            throw new BadRequestException("Los datos del médico y su especialización son obligatorios");
        }

        if (actualizar && (solicitud.getId() == null || !medicoRepository.existsById(solicitud.getId()))) {
            throw new BadRequestException("El médico que desea actualizar no existe");
        }
        if (medicoRepository.findByTipoDocumentoAndNumeroDocumento(
                solicitud.getTipoDocumento(), solicitud.getNumeroDocumento())
                .filter(existing -> !actualizar || !existing.getId().equals(solicitud.getId())).isPresent()) {
            throw new BadRequestException("Ya existe un médico con ese documento");
        }
        if (medicoRepository.findByRegistroProfesional(solicitud.getRegistroProfesional())
                .filter(existing -> !actualizar || !existing.getId().equals(solicitud.getId())).isPresent()) {
            throw new BadRequestException("Ya existe un médico con ese registro profesional");
        }

        Especializacion especializacion = especializacionRepository.findById(solicitud.getEspecializacionId())
                .orElseThrow(() -> new BadRequestException("La especialización seleccionada no existe"));
        Medico medico = new Medico();
        medico.setId(actualizar ? solicitud.getId() : null);
        medico.setTipoDocumento(solicitud.getTipoDocumento());
        medico.setNumeroDocumento(solicitud.getNumeroDocumento());
        medico.setNombres(solicitud.getNombres());
        medico.setApellidos(solicitud.getApellidos());
        medico.setTelefono(solicitud.getTelefono());
        medico.setRegistroProfesional(solicitud.getRegistroProfesional());
        medico.setEspecializacion(especializacion);
        return medicoRepository.save(medico);
    }
}
