package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.EspecializacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de la lógica de negocio del catálogo de especializaciones.
 */
@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Override
    public List<Especializacion> listarEspecializaciones() {
        return especializacionRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public Especializacion buscarEspecializacionPorCodigo(String codigo) throws BadRequestException {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new BadRequestException("El código de la especialización no puede estar vacío");
        }
        return especializacionRepository.findByCodigoEspecializacion(codigo.trim())
                .orElseThrow(() -> new BadRequestException("No se encontró la especialización con el código: " + codigo, HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(especializacionRq);

        // Paso 2. Evito nombres y códigos duplicados
        this.validarUnicidad(especializacionRq, null);

        // Paso 3. Creo la entidad a guardar
        Especializacion especializacion = new Especializacion();
        this.cargarDatos(especializacion, especializacionRq);
        this.especializacionRepository.save(especializacion);

        // Paso 4. Retorno la respuesta
        return respuesta("Especialización guardada correctamente");
    }

    @Override
    public MiRespuestaRS actualizarEspecializacion(EspecializacionRq especializacionRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(especializacionRq);
        if (especializacionRq.getId() == null || especializacionRq.getId() <= 0) {
            throw new BadRequestException("El ID de la especialización no puede estar vacío ni ser menor o igual a cero");
        }

        // Paso 2. Verifico que exista y que no haya duplicados en otro registro
        Especializacion especializacion = this.especializacionRepository.findById(especializacionRq.getId())
                .orElseThrow(() -> new BadRequestException("La especialización con ID " + especializacionRq.getId() + " no existe", HttpStatus.NOT_FOUND));
        this.validarUnicidad(especializacionRq, especializacion.getId());

        // Paso 3. Actualizo los datos
        this.cargarDatos(especializacion, especializacionRq);
        this.especializacionRepository.save(especializacion);

        // Paso 4. Retorno la respuesta
        return respuesta("Especialización actualizada correctamente");
    }

    private void validarUnicidad(EspecializacionRq rq, Long idActual) {
        if (especializacionRepository.findByCodigoEspecializacion(rq.getCodigoEspecializacion().trim())
                .filter(otra -> !otra.getId().equals(idActual)).isPresent()) {
            throw new BadRequestException("Ya existe una especialización con el código " + rq.getCodigoEspecializacion(), HttpStatus.CONFLICT);
        }
        if (especializacionRepository.findByNombre(rq.getNombre().trim())
                .filter(otra -> !otra.getId().equals(idActual)).isPresent()) {
            throw new BadRequestException("Ya existe una especialización con el nombre " + rq.getNombre(), HttpStatus.CONFLICT);
        }
    }

    private void cargarDatos(Especializacion especializacion, EspecializacionRq rq) {
        especializacion.setNombre(rq.getNombre().trim());
        especializacion.setDescripcion(rq.getDescripcion());
        especializacion.setCodigoEspecializacion(rq.getCodigoEspecializacion().trim());
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(EspecializacionRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (rq.getNombre() == null || rq.getNombre().isBlank()) {
            throw new BadRequestException("El nombre de la especialización es obligatorio");
        }
        if (rq.getCodigoEspecializacion() == null || rq.getCodigoEspecializacion().isBlank()) {
            throw new BadRequestException("El código de la especialización es obligatorio");
        }
    }
}
