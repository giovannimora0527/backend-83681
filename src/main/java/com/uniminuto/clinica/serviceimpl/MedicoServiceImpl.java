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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio de médicos.
 */
@Service
public class MedicoServiceImpl implements MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Override
    public List<Medico> listarMedicos() {
        return medicoRepository.findAllByOrderByApellidosAscNombresAsc();
    }

    @Override
    public Medico buscarMedico(String tipoDocumento, String numeroDocumento) throws BadRequestException {
        if (tipoDocumento == null || tipoDocumento.isBlank() || numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new BadRequestException("El tipo y número de documento son obligatorios");
        }
        return medicoRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento.trim(), numeroDocumento.trim())
                .orElseThrow(() -> new BadRequestException("Médico no encontrado", HttpStatus.NOT_FOUND));
    }

    @Override
    public Medico buscarMedicoPorRegistroProfesional(String registroProfesional) throws BadRequestException {
        if (registroProfesional == null || registroProfesional.isBlank()) {
            throw new BadRequestException("El registro profesional es obligatorio");
        }
        return medicoRepository.findByRegistroProfesional(registroProfesional.trim())
                .orElseThrow(() -> new BadRequestException("Médico no encontrado", HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(medicoRq);

        // Paso 2. Verifico que la especialización exista
        Especializacion especializacion = this.obtenerEspecializacion(medicoRq.getEspecializacionId());

        // Paso 3. Evito documentos y registros profesionales duplicados
        this.validarUnicidad(medicoRq, null);

        // Paso 4. Creo la entidad médico a guardar
        Medico medico = new Medico();
        this.cargarDatos(medico, medicoRq, especializacion);
        this.medicoRepository.save(medico);

        // Paso 5. Retorno la respuesta
        return respuesta("Médico guardado correctamente");
    }

    @Override
    public MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(medicoRq);
        if (medicoRq.getId() == null || medicoRq.getId() <= 0) {
            throw new BadRequestException("El ID del médico no puede estar vacío ni ser menor o igual a cero");
        }

        // Paso 2. Verifico que el médico y la especialización existan
        Medico medico = this.medicoRepository.findById(medicoRq.getId())
                .orElseThrow(() -> new BadRequestException("El médico con ID " + medicoRq.getId() + " no existe", HttpStatus.NOT_FOUND));
        Especializacion especializacion = this.obtenerEspecializacion(medicoRq.getEspecializacionId());

        // Paso 3. Evito duplicados en otros registros
        this.validarUnicidad(medicoRq, medico.getId());

        // Paso 4. Actualizo los datos
        this.cargarDatos(medico, medicoRq, especializacion);
        this.medicoRepository.save(medico);

        // Paso 5. Retorno la respuesta
        return respuesta("Médico actualizado correctamente");
    }

    private Especializacion obtenerEspecializacion(Long especializacionId) {
        return this.especializacionRepository.findById(especializacionId)
                .orElseThrow(() -> new BadRequestException("La especialización con ID " + especializacionId + " no existe", HttpStatus.NOT_FOUND));
    }

    private void validarUnicidad(MedicoRq rq, Long idActual) {
        if (medicoRepository.findByTipoDocumentoAndNumeroDocumento(rq.getTipoDocumento().trim(), rq.getNumeroDocumento().trim())
                .filter(otro -> !otro.getId().equals(idActual)).isPresent()) {
            throw new BadRequestException("Ya existe un médico con ese documento", HttpStatus.CONFLICT);
        }
        if (medicoRepository.findByRegistroProfesional(rq.getRegistroProfesional().trim())
                .filter(otro -> !otro.getId().equals(idActual)).isPresent()) {
            throw new BadRequestException("Ya existe un médico con ese registro profesional", HttpStatus.CONFLICT);
        }
    }

    private void cargarDatos(Medico medico, MedicoRq rq, Especializacion especializacion) {
        medico.setTipoDocumento(rq.getTipoDocumento().trim());
        medico.setNumeroDocumento(rq.getNumeroDocumento().trim());
        medico.setNombres(rq.getNombres().trim());
        medico.setApellidos(rq.getApellidos().trim());
        medico.setTelefono(rq.getTelefono());
        medico.setRegistroProfesional(rq.getRegistroProfesional().trim());
        medico.setEspecializacion(especializacion);
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(MedicoRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (rq.getTipoDocumento() == null || rq.getTipoDocumento().isBlank()
                || rq.getNumeroDocumento() == null || rq.getNumeroDocumento().isBlank()) {
            throw new BadRequestException("El tipo y número de documento del médico son obligatorios");
        }
        if (rq.getNombres() == null || rq.getNombres().isBlank()
                || rq.getApellidos() == null || rq.getApellidos().isBlank()) {
            throw new BadRequestException("Los nombres y apellidos del médico son obligatorios");
        }
        if (rq.getRegistroProfesional() == null || rq.getRegistroProfesional().isBlank()) {
            throw new BadRequestException("El registro profesional es obligatorio");
        }
        if (rq.getEspecializacionId() == null || rq.getEspecializacionId() <= 0) {
            throw new BadRequestException("La especialización del médico es obligatoria");
        }
    }
}
