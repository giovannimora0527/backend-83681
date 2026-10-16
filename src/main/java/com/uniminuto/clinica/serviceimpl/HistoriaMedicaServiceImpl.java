package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;
    @Autowired
    private MascotaRepository mascotaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private MedicoRepository medicoRepository;

    private void validarHistoriaMedicaRq(HistoriaMedicaRq request) throws BadRequestException {

        // Datos obligatorios
        if (request == null) {
            throw new BadRequestException("Los datos del formulario son obligatorios");
        }

        // ID del cliente obligatorio
        if (request.getClienteId() == null) {
            throw new BadRequestException("El ID del cliente es obligatorio");
        }

        // ID de la mascota obligatorio
        if (request.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota es obligatorio");
        }

        // ID de la cita obligatorio
        if (request.getCitaId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio");
        }

        // ID del médico obligatorio
        if (request.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }

        // El motivo de consulta obligatorio
        if (request.getMotivoConsulta() == null || request.getMotivoConsulta().trim().isEmpty()) {
            throw new BadRequestException("El motivo de consulta es obligatorio");
        }

        // El diagnóstico es obligatorio
        if (request.getDiagnostico() == null || request.getDiagnostico().trim().isEmpty()) {
            throw new BadRequestException("El diagnóstico es obligatorio");
        }
    }

    @Override
    public List<HistoriaMedica> listarHistorias() throws BadRequestException {
        try {
            // Ordenar por fecha descendente
            return historiaMedicaRepository.findAllByOrderByFechaCreacionDesc();
        } catch (Exception e) {
            throw new BadRequestException("Error al listar las historias clínicas: " + e.getMessage());
        }
    }

    @Override
    public MiRespuestaRS crearHistoria(HistoriaMedicaRq request) throws BadRequestException {

        // Validaciones obligatorias
        this.validarHistoriaMedicaRq(request);

        // Validar la existencia del cliente y la mascota
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new BadRequestException("Error: El cliente no existe."));

        Mascota mascota = mascotaRepository.findById(request.getMascotaId().intValue())
                .orElseThrow(() -> new BadRequestException("Error: La mascota no existe."));

        // Validar que la mascota pertenezca al cliente seleccionado
        Long clienteIdDeLaMascota = mascota.getCliente().getId();
        Long clienteIdPeticion = cliente.getId();

        if (!clienteIdDeLaMascota.equals(clienteIdPeticion)) {
            throw new BadRequestException("La mascota '" + mascota.getNombreMascota() + "' NO pertenece al cliente '" + cliente.getNombres() + "'.");
        }

        // Validar existencia de la cita y el médico
        Cita cita = citaRepository.findById(request.getCitaId())
                .orElseThrow(() -> new BadRequestException("Error: La cita no existe."));

        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new BadRequestException("Error: El veterinario no existe."));

        // Crear y guardar la historia médica
        HistoriaMedica historia = new HistoriaMedica();
        historia.setMascota(mascota);
        historia.setCita(cita);
        historia.setMedico(medico);
        historia.setFechaCreacion(LocalDateTime.now());
        historia.setMotivoConsulta(request.getMotivoConsulta());
        historia.setDiagnostico(request.getDiagnostico());
        historia.setObservaciones(request.getObservaciones());
        historia.setEstado("Abierta");

        historiaMedicaRepository.save(historia);

        // Respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Historia clínica creada exitosamente.");
        return respuesta;
    }
}