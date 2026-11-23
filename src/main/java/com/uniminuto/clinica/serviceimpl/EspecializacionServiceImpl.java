package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.exception.BadRequestException;
<<<<<<< HEAD
=======
import com.uniminuto.clinica.models.EspecializacionRq;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.service.EspecializacionService;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
=======
import org.springframework.http.HttpStatus;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de la lógica de negocio del catálogo de especializaciones.
 */
@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    @Autowired
    private EspecializacionRepository especializacionRepository;

    @Override
    public List<Especializacion> listarEspecializaciones() {
<<<<<<< HEAD
        return especializacionRepository.findAll().stream()
                .sorted((e1, e2) -> e1.getNombre()
                        .compareToIgnoreCase(e2.getNombre())).toList();
    }

    @Override
    public MiRespuestaRS crearEspecializacion(Especializacion especializacion) throws BadRequestException {
        this.validarEspecializacion(especializacion);
        Optional<Especializacion> especializacionExistente = especializacionRepository
                .findByCodigoEspecializacion(especializacion.getCodigoEspecializacion());
        if (especializacionExistente.isPresent()) {
            throw new BadRequestException("La especialización con el código proporcionado ya existe.");
        }

        Optional<Especializacion> especializacionPorNombre = especializacionRepository
                .findByNombre(especializacion.getNombre());
        if (especializacionPorNombre.isPresent()) {
            throw new BadRequestException("La especialización con el nombre proporcionado ya existe.");
        }

        especializacionRepository.save(especializacion);
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Especialización creada exitosamente.");
        rta.setStatus(200);
        return rta;
    }

    private void validarEspecializacion(Especializacion especializacion) throws BadRequestException {
        if (especializacion.getCodigoEspecializacion() == null || especializacion.getCodigoEspecializacion().isEmpty()) {
            throw new BadRequestException("El código de la especialización no puede estar vacío.");
        }
        if (especializacion.getNombre() == null || especializacion.getNombre().isEmpty()) {
            throw new BadRequestException("El nombre de la especialización no puede estar vacío.");
        }
        if (especializacion.getDescripcion() == null || especializacion.getDescripcion().isEmpty()) {
            throw new BadRequestException("La descripción de la especialización no puede estar vacía.");
        }
    }

    @Override
    public MiRespuestaRS actualizarEspecializacion(Especializacion especializacion) throws BadRequestException {
        this.validarEspecializacion(especializacion);
        Optional<Especializacion> especializacionExistente = especializacionRepository
                .findById(especializacion.getId());
        if (especializacionExistente.isEmpty()) {
            throw new BadRequestException("La especialización que se intenta actualizar no existe.");
        }
        Especializacion especializacionActual = especializacionExistente.get();
        if (especializacionActual.getCodigoEspecializacion().equals(especializacion.getCodigoEspecializacion())) {
            Optional<Especializacion> especializacionPorCodigo = especializacionRepository
                    .findByCodigoEspecializacion(especializacion.getCodigoEspecializacion());
            if (especializacionPorCodigo.isPresent() && !especializacionPorCodigo.get().getId()
                    .equals(especializacion.getId())) {
                throw new BadRequestException("La especialización con el código proporcionado ya existe.");
            }
        }

        if (especializacionActual.getNombre().equals(especializacion.getNombre())) {
            Optional<Especializacion> especializacionPorNombre = especializacionRepository
                    .findByNombre(especializacion.getNombre());
            if (especializacionPorNombre.isPresent() && !especializacionPorNombre.get().getId()
                    .equals(especializacion.getId())) {
                throw new BadRequestException("La especialización con el nombre proporcionado ya existe.");
            }
=======
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
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
        }

        especializacionActual.setCodigoEspecializacion(especializacion.getCodigoEspecializacion());
        especializacionActual.setNombre(especializacion.getNombre());
        especializacionActual.setDescripcion(especializacion.getDescripcion());
        especializacionRepository.save(especializacionActual);
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Especialización actualizada exitosamente.");
        rta.setStatus(200);
        return rta;
    }
}
