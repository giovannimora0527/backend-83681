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
import java.util.Locale;

/** Valida los datos del catálogo de especialidades y guarda su estado activo. */
@Service
public class EspecializacionServiceImpl implements EspecializacionService {

    @Autowired
    private EspecializacionRepository especializacionRepository;

    /** Consulta las especialidades veterinarias. */
    @Override
    public List<Especializacion> listarEspecializaciones() {
        return especializacionRepository.findAll().stream()
                .sorted((e1, e2) -> e1.getNombre()
                        .compareToIgnoreCase(e2.getNombre())).toList();
    }

    /** Busca las especialidades veterinarias. */
    @Override
    public List<Especializacion> buscarEspecializaciones(String termino) {
        String busqueda = termino == null ? "" : termino.trim().toLowerCase(Locale.ROOT);
        return listarEspecializaciones().stream()
                .filter(item -> busqueda.isEmpty()
                        || contiene(item.getNombre(), busqueda)
                        || contiene(item.getDescripcion(), busqueda)
                        || contiene(item.getCodigoEspecializacion(), busqueda))
                .toList();
    }

    /** Comprueba si el valor contiene el texto buscado. */
    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    /** Registra la especialidad veterinaria. */
    @Override
    public MiRespuestaRS crearEspecializacion(EspecializacionRq solicitud) {
        validarEspecializacion(solicitud);
        var nueva = new Especializacion();
        nueva.setActivo(Boolean.TRUE);
        guardar(nueva, solicitud);
        return respuesta("Especialización creada exitosamente.");
    }

    /** Actualiza la especialidad veterinaria. */
    @Override
    public MiRespuestaRS actualizarEspecializacion(EspecializacionRq solicitud) {
        validarEspecializacion(solicitud);
        if (solicitud.id() == null || solicitud.id() <= 0) {
            throw new BadRequestException("El identificador de la especialización es obligatorio.");
        }
        var especializacion = especializacionRepository.findById(solicitud.id())
                .orElseThrow(() -> new BadRequestException("La especialización que se intenta actualizar no existe."));
        guardar(especializacion, solicitud);
        return respuesta("Especialización actualizada exitosamente.");
    }

    /** Valida la especialidad veterinaria. */
    private void validarEspecializacion(EspecializacionRq solicitud) {
        if (solicitud == null || solicitud.codigoEspecializacion() == null
                || solicitud.codigoEspecializacion().isBlank() || solicitud.codigoEspecializacion().strip().length() > 10) {
            throw new BadRequestException("El código de la especialización es obligatorio y admite hasta 10 caracteres.");
        }
        if (solicitud.nombre() == null || solicitud.nombre().isBlank() || solicitud.nombre().strip().length() > 100) {
            throw new BadRequestException("El nombre de la especialización es obligatorio y admite hasta 100 caracteres.");
        }
        if (solicitud.descripcion() == null || solicitud.descripcion().isBlank()) {
            throw new BadRequestException("La descripción de la especialización no puede estar vacía.");
        }
        if (solicitud.descripcion().strip().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 65535) {
            throw new BadRequestException("La descripción de la especialización supera el tamaño permitido.");
        }
    }

    /** Guarda la especialidad. */
    private void guardar(Especializacion especializacion, EspecializacionRq solicitud) {
        String codigo = solicitud.codigoEspecializacion().strip();
        String nombre = solicitud.nombre().strip();
        if (especializacionRepository.findByCodigoEspecializacion(codigo)
                .filter(actual -> !actual.getId().equals(especializacion.getId())).isPresent()) {
            throw new BadRequestException("La especialización con el código proporcionado ya existe.");
        }
        if (especializacionRepository.findByNombre(nombre)
                .filter(actual -> !actual.getId().equals(especializacion.getId())).isPresent()) {
            throw new BadRequestException("La especialización con el nombre proporcionado ya existe.");
        }
        especializacion.setCodigoEspecializacion(codigo);
        especializacion.setNombre(nombre);
        especializacion.setDescripcion(solicitud.descripcion().strip());
        if (solicitud.activo() != null) {
            especializacion.setActivo(solicitud.activo());
        }
        especializacionRepository.save(especializacion);
    }

    /** Prepara el mensaje que devuelve la operación. */
    private MiRespuestaRS respuesta(String mensaje) {
        var respuesta = new MiRespuestaRS();
        respuesta.setMessage(mensaje);
        respuesta.setStatus(200);
        return respuesta;
    }
}
