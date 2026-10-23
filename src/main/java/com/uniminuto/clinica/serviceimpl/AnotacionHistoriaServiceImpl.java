package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.models.AnotacionRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import com.uniminuto.clinica.service.HistoriaClinicaService;
import com.uniminuto.clinica.util.RespuestaUtil;
import com.uniminuto.clinica.util.ValidadorEntidades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Logica de negocio de las anotaciones de historia clinica.
 *
 * Reglas implementadas:
 * - La historia clinica debe existir y estar ABIERTA.
 * - El veterinario debe existir.
 * - Descripcion y diagnostico/evolucion son obligatorios.
 * - La fecha la asigna el servidor (no se puede falsear desde el frontend).
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /**
     * Repositorio de anotaciones.
     */
    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * Servicio de historias (se reutiliza su metodo consultarHistoria).
     */
    @Autowired
    private HistoriaClinicaService historiaClinicaService;

    /**
     * Validaciones comunes (veterinario, mascota, textos obligatorios).
     */
    @Autowired
    private ValidadorEntidades validador;

    @Override
    public MiRespuestaRS crearAnotacionHistoria(AnotacionRq anotacionRq) throws BadRequestException {
        // Paso 1. Campos obligatorios
        if (anotacionRq == null) {
            throw new BadRequestException("Los datos de la anotación son obligatorios.");
        }
        validador.validarTextoObligatorio(anotacionRq.getDescripcion(), "descripción");
        validador.validarTextoObligatorio(anotacionRq.getDiagnostico(), "diagnóstico o evolución");

        // Paso 2. La historia existe (404 si no) y esta abierta
        HistoriaMedica historia = historiaClinicaService.consultarHistoria(anotacionRq.getHistoriaId());
        if (HistoriaClinicaServiceImpl.ESTADO_CERRADA.equals(historia.getEstado())) {
            throw new ConflictException("La historia clínica está CERRADA; no se pueden agregar anotaciones.");
        }

        // Paso 3. El veterinario existe
        Medico medico = validador.obtenerMedico(anotacionRq.getMedicoId());

        // Paso 4. Guardar con la fecha actual del servidor
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoria(historia);
        anotacion.setMedico(medico);
        anotacion.setFecha(LocalDateTime.now());
        anotacion.setDescripcion(anotacionRq.getDescripcion().trim());
        anotacion.setObservacion(anotacionRq.getObservacion());
        anotacion.setDiagnostico(anotacionRq.getDiagnostico().trim());
        anotacionHistoriaRepository.save(anotacion);

        return RespuestaUtil.crear(201, "Anotación agregada correctamente a la historia de "
                + historia.getMascota().getNombreMascota() + ".");
    }

    @Override
    public List<AnotacionHistoria> listarPorHistoria(Long historiaId) throws BadRequestException {
        historiaClinicaService.consultarHistoria(historiaId);
        return anotacionHistoriaRepository.findByHistoriaIdOrderByFechaDesc(historiaId);
    }

    @Override
    public List<AnotacionHistoria> listarPorMascota(Long mascotaId) throws BadRequestException {
        validador.obtenerMascota(mascotaId);
        return anotacionHistoriaRepository.findByHistoriaMascotaMascotaIdOrderByFechaDesc(mascotaId);
    }
}
