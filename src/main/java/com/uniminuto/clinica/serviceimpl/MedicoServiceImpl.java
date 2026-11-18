package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.CuentaMedicoRs;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.repository.MedicoUsuarioRepository;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Locale;

/**
 * Administra el directorio veterinario y verifica que cada cuenta vinculada corresponda a un solo
 * médico.
 */
@Service
public class MedicoServiceImpl implements MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Autowired
    private MedicoUsuarioRepository medicoUsuarioRepository;

    private static final String ESTADO_CITA = "PROGRAMADA";

    @Override
    public List<CuentaMedicoRs> listarCuentasDisponibles() {
        return medicoUsuarioRepository.listarDisponibles();
    }

    @Override
    public Optional<CuentaMedicoRs> consultarCuenta(Long medicoId) {
        if (!medicoRepository.existsById(medicoId)) {
            throw new BadRequestException("El médico no existe.");
        }
        return medicoUsuarioRepository.buscarCuenta(medicoId);
    }

    @Override
    @Transactional
    public CuentaMedicoRs vincularCuenta(Long medicoId, Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new BadRequestException("Indica un identificador de usuario válido.");
        }
        exigirMedicoBloqueado(medicoId);
        CuentaMedicoRs cuenta = medicoUsuarioRepository.bloquearCuentaMedica(usuarioId)
                .orElseThrow(() -> new BadRequestException("El usuario debe tener una cuenta activa con perfil médico."));
        var vinculo = medicoUsuarioRepository.bloquearVinculoPorUsuario(usuarioId);
        if (vinculo.isPresent()) {
            if (!vinculo.get().equals(medicoId)) {
                throw new BadRequestException("La cuenta ya está vinculada a otro médico.");
            }
            return cuenta;
        }
        medicoUsuarioRepository.vincular(medicoId, usuarioId);
        return cuenta;
    }

    @Override
    @Transactional
    public void desvincularCuenta(Long medicoId) {
        exigirMedicoBloqueado(medicoId);
        medicoUsuarioRepository.desvincular(medicoId);
    }

    private void exigirMedicoBloqueado(Long medicoId) {
        if (!medicoUsuarioRepository.bloquearMedico(medicoId)) {
            throw new BadRequestException("El médico no existe.");
        }
    }

    @Override
    public List<Medico> listarMedicos() {
        return this.medicoRepository.findAll().stream().sorted((m1, m2) -> m1.getApellidos()
                .compareToIgnoreCase(m2.getApellidos())).toList();
    }

    @Override
    public List<Medico> buscarMedicos(String termino) {
        String busqueda = termino == null ? "" : termino.trim().toLowerCase(Locale.ROOT);
        return listarMedicos().stream()
                .filter(medico -> busqueda.isEmpty()
                        || contiene(medico.getNombres(), busqueda)
                        || contiene(medico.getApellidos(), busqueda)
                        || contiene(medico.getNumeroDocumento(), busqueda)
                        || contiene(medico.getRegistroProfesional(), busqueda)
                        || (medico.getEspecializacion() != null
                        && contiene(medico.getEspecializacion().getNombre(), busqueda)))
                .toList();
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    @Override
    public List<Medico> listarMedicosDisponiblesPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin)
            throws BadRequestException {

        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas no pueden ser nulas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio no puede ser mayor a la fecha de fin");
        }

        return this.medicoRepository.findDisponiblesPorFecha(fechaInicio, fechaFin, ESTADO_CITA);
    }

    @Override
    public MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException {
        this.validarMedico(medicoRq);
        if (this.medicoRepository.existsByNumeroDocumento(medicoRq.getNumeroDocumento().trim())) {
            throw new BadRequestException("Ya existe un médico con el número de documento proporcionado.");
        }
        if (this.medicoRepository.existsByRegistroProfesional(medicoRq.getRegistroProfesional().trim())) {
            throw new BadRequestException("Ya existe un médico con el registro profesional proporcionado.");
        }
        Especializacion especializacion = this.existeEspecializacion(medicoRq);

        Medico medicoNuevo = new Medico();
        this.mapearMedico(medicoNuevo, medicoRq, especializacion);
        medicoNuevo.setEstado(Boolean.TRUE);
        this.medicoRepository.save(medicoNuevo);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Médico guardado correctamente.");
        return rta;
    }

    @Override
    public MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException {
        if (medicoRq == null || medicoRq.getId() == null || medicoRq.getId() <= 0) {
            throw new BadRequestException("El id del médico es obligatorio y debe ser mayor a cero.");
        }
        this.validarMedico(medicoRq);
        Optional<Medico> optionalMedico = this.medicoRepository.findById(medicoRq.getId());
        if (optionalMedico.isEmpty()) {
            throw new BadRequestException("El médico con el id proporcionado no existe.");
        }
        if (this.medicoRepository.existsByNumeroDocumentoAndIdNot(
                medicoRq.getNumeroDocumento().trim(), medicoRq.getId())) {
            throw new BadRequestException("Ya existe otro médico con el número de documento proporcionado.");
        }
        if (this.medicoRepository.existsByRegistroProfesionalAndIdNot(
                medicoRq.getRegistroProfesional().trim(), medicoRq.getId())) {
            throw new BadRequestException("Ya existe otro médico con el registro profesional proporcionado.");
        }
        Especializacion especializacion = this.existeEspecializacion(medicoRq);

        Medico medicoExistente = optionalMedico.get();
        this.mapearMedico(medicoExistente, medicoRq, especializacion);
        this.medicoRepository.save(medicoExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Médico actualizado correctamente.");
        return rta;
    }

    private void mapearMedico(Medico medico, MedicoRq medicoRq, Especializacion especializacion) {
        medico.setTipoDocumento(medicoRq.getTipoDocumento().trim());
        medico.setNumeroDocumento(medicoRq.getNumeroDocumento().trim());
        medico.setNombres(medicoRq.getNombres().trim());
        medico.setApellidos(medicoRq.getApellidos().trim());
        medico.setTelefono(medicoRq.getTelefono() == null ? null : medicoRq.getTelefono().trim());
        medico.setRegistroProfesional(medicoRq.getRegistroProfesional().trim());
        medico.setEspecializacion(especializacion);
    }

    private Especializacion existeEspecializacion(MedicoRq medicoRq) throws BadRequestException {
        Optional<Especializacion> optionalEspecializacion = this.especializacionRepository
                .findById(medicoRq.getEspecializacionId());
        if (optionalEspecializacion.isEmpty()) {
            throw new BadRequestException("La especialización con el id proporcionado no existe.");
        }
        return optionalEspecializacion.get();
    }

    private void validarMedico(MedicoRq medicoRq) throws BadRequestException {
        if (medicoRq == null) {
            throw new BadRequestException("La información del médico es obligatoria.");
        }
        if (this.esVacio(medicoRq.getTipoDocumento())) {
            throw new BadRequestException("El tipo de documento es obligatorio.");
        }
        if (this.esVacio(medicoRq.getNumeroDocumento())) {
            throw new BadRequestException("El número de documento es obligatorio.");
        }
        if (this.esVacio(medicoRq.getNombres())) {
            throw new BadRequestException("Los nombres del médico son obligatorios.");
        }
        if (this.esVacio(medicoRq.getApellidos())) {
            throw new BadRequestException("Los apellidos del médico son obligatorios.");
        }
        if (this.esVacio(medicoRq.getRegistroProfesional())) {
            throw new BadRequestException("El registro profesional es obligatorio.");
        }
        if (medicoRq.getEspecializacionId() == null || medicoRq.getEspecializacionId() <= 0) {
            throw new BadRequestException("El id de la especialización es obligatorio y debe ser mayor a cero.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
