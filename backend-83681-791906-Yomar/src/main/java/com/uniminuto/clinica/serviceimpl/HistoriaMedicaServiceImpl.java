package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    /**
     * Repositorio JPA para acceder a los datos de las historias médicas.
     */
    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Repositorio JPA usado para validar que el paciente (mascota) exista.
     */
    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public MiRespuestaRS crearHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        // Paso 1. Validar los campos obligatorios del objeto de entrada.
        this.validarObjetoEntrada(historiaMedicaRq, true);

        // Paso 2. Construir la historia médica con el paciente ya validado.
        HistoriaMedica historiaMedica = new HistoriaMedica();
        historiaMedica.setPaciente(this.obtenerPaciente(historiaMedicaRq.getPacienteId()));
        historiaMedica.setFechaCreacion(LocalDateTime.now());

        this.historiaMedicaRepository.save(historiaMedica);

        // Paso 3. Retornar la respuesta de la operación.
        return this.construirRespuesta("Historia médica creada correctamente");
    }

    @Override
    public List<HistoriaMedica> listarHistoriasMedicas(LocalDateTime fechaInicial, LocalDateTime fechaFinal) throws BadRequestException {
        // Paso 1. Validar que ambas fechas hayan sido enviadas.
        if (fechaInicial == null || fechaFinal == null) {
            throw new BadRequestException("La fecha inicial y la fecha final son obligatorias");
        }

        // Paso 2. Validar la coherencia del rango de fechas.
        if (fechaInicial.isAfter(fechaFinal)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }

        // Paso 3. Consultar las historias del rango, ya ordenadas descendentemente.
        return this.historiaMedicaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicial, fechaFinal);
    }

    @Override
    public MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        // Paso 1. Validar los campos obligatorios, incluyendo el identificador.
        this.validarObjetoEntrada(historiaMedicaRq, false);

        // Paso 2. Validar que la historia médica a actualizar exista.
        Optional<HistoriaMedica> optHistoria = this.historiaMedicaRepository.findById(historiaMedicaRq.getId());
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia médica con ID " + historiaMedicaRq.getId() + " no existe en la base de datos");
        }

        // Paso 3. Actualizar el paciente asociado a la historia médica.
        HistoriaMedica historiaMedica = optHistoria.get();
        historiaMedica.setPaciente(this.obtenerPaciente(historiaMedicaRq.getPacienteId()));

        this.historiaMedicaRepository.save(historiaMedica);

        // Paso 4. Retornar la respuesta de la operación.
        return this.construirRespuesta("Historia médica actualizada correctamente");
    }

    /**
     * Busca un paciente (mascota) por su identificador y valida que exista.
     */
    private Mascota obtenerPaciente(Long pacienteId) throws BadRequestException {
        return this.mascotaRepository.findById(pacienteId)
                .orElseThrow(() -> new BadRequestException("El paciente con ID " + pacienteId + " no existe en la base de datos"));
    }

    /**
     * Construye una respuesta exitosa estándar para las operaciones del servicio.
     */
    private MiRespuestaRS construirRespuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    /**
     * Valida los campos obligatorios de una solicitud de historia médica.
     */
    private void validarObjetoEntrada(HistoriaMedicaRq historiaMedicaRq, boolean esCreacion) throws BadRequestException {
        if (historiaMedicaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }

        if (!esCreacion && historiaMedicaRq.getId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio para actualizar");
        }

        if (historiaMedicaRq.getPacienteId() == null) {
            throw new BadRequestException("El ID del paciente no puede estar vacío");
        }
    }
}