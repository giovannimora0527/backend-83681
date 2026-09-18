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

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Override
    public List<HistoriaMedica> listarHistoriasMedicas() throws BadRequestException {
        return historiaMedicaRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public MiRespuestaRS guardarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(historiaMedicaRq);

        // Paso 2. Validar que el paciente (mascota) exista
        Mascota paciente = this.buscarPaciente(historiaMedicaRq.getPacienteId());

        // Paso 3. Crear la historia médica
        HistoriaMedica historiaMedica = new HistoriaMedica();
        historiaMedica.setPaciente(paciente);
        historiaMedica.setFechaCreacion(LocalDateTime.now());

        this.historiaMedicaRepository.save(historiaMedica);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia médica guardada correctamente");
        return respuesta;
    }

    @Override
    public MiRespuestaRS actualizarHistoriaMedica(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(historiaMedicaRq);

        if (historiaMedicaRq.getId() == null) {
            throw new BadRequestException("El ID de la historia médica es obligatorio para actualizar");
        }

        // Paso 2. Validar que la historia médica y el paciente existan
        Optional<HistoriaMedica> optHistoria = this.historiaMedicaRepository.findById(historiaMedicaRq.getId());
        if (optHistoria.isEmpty()) {
            throw new BadRequestException("La historia médica con ID " + historiaMedicaRq.getId() + " no existe en la base de datos");
        }

        Mascota paciente = this.buscarPaciente(historiaMedicaRq.getPacienteId());

        // Paso 3. Actualizar los datos de la historia médica
        HistoriaMedica historiaMedica = optHistoria.get();
        historiaMedica.setPaciente(paciente);

        this.historiaMedicaRepository.save(historiaMedica);

        // Paso 4. Retornar la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia médica actualizada correctamente");
        return respuesta;
    }

    /**
     * Valida los campos obligatorios del objeto de entrada de una historia médica.
     */
    private void validarObjetoEntrada(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        if (historiaMedicaRq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (historiaMedicaRq.getPacienteId() == null) {
            throw new BadRequestException("El ID del paciente es obligatorio");
        }
    }

    /**
     * Busca el paciente (mascota) por id o lanza BadRequestException si no existe.
     */
    private Mascota buscarPaciente(Long pacienteId) throws BadRequestException {
        Optional<Mascota> optMascota = this.mascotaRepository.findById(pacienteId);
        if (optMascota.isEmpty()) {
            throw new BadRequestException("El paciente (mascota) con ID " + pacienteId + " no existe en la base de datos");
        }
        return optMascota.get();
    }
}