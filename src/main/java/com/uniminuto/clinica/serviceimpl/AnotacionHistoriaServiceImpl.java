package com.uniminuto.clinica.serviceimpl;

<<<<<<< HEAD
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
=======
import com.uniminuto.clinica.models.AnotacionVistaRS;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.uniminuto.clinica.util.Especificaciones;
import com.uniminuto.clinica.util.Paginacion;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
<<<<<<< HEAD
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

=======
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import com.uniminuto.clinica.util.FechaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de la lógica de negocio de anotaciones de historia clínica.
 */
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
<<<<<<< HEAD
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;
=======
    private AnotacionHistoriaRepository anotacionRepository;

    @Autowired
    private HistoriaMedicaRepository historiaRepository;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

    @Autowired
    private MedicoRepository medicoRepository;

<<<<<<< HEAD
    @Autowired
    private CitaRepository citaRepository;

    @Override
    @Transactional
    public MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) {
        this.validarAnotacionHistoria(anotacionHistoriaRq);
        Optional<Cita> optCita = this.citaRepository.findById(anotacionHistoriaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + anotacionHistoriaRq.getCitaId()  +
                    " no existe. Por favor consulte al administrador del sistema o registre la cita antes de " +
                    "crear la anotación de historia clínica.");
        }

        HistoriaMedica historia = new HistoriaMedica();
        AnotacionHistoria nuevaAnotacion = new AnotacionHistoria();

        Cita cita = optCita.get();

        Optional<HistoriaMedica> historiaOpt = this.historiaMedicaRepository
                .findByMascota(cita.getMascota());
        boolean existeHistoria = historiaOpt.isPresent();
        if (historiaOpt.isEmpty()) {
           HistoriaMedica nueva = new HistoriaMedica();
           nueva.setMascota(cita.getMascota());
           nueva.setFechaCreacion(LocalDateTime.now());
           historia = this.historiaMedicaRepository.save(nueva);
        }

        Optional<Medico> medicoOptional = this.medicoRepository.findById(anotacionHistoriaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico con ID " + anotacionHistoriaRq.getMedicoId() + " no existe");
        }
        nuevaAnotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        nuevaAnotacion.setHistoria(existeHistoria? historiaOpt.get() : historia);
        nuevaAnotacion.setDiagnostico(anotacionHistoriaRq.getDiagnostico());
        nuevaAnotacion.setObservaciones(anotacionHistoriaRq.getObservaciones());
        nuevaAnotacion.setMedico(medicoOptional.get());
        nuevaAnotacion.setFecha(LocalDateTime.now());
        nuevaAnotacion.setCita(cita);
        this.anotacionHistoriaRepository.save(nuevaAnotacion);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Anotación de historia clínica creada exitosamente");
        return rta;
    }

    private void validarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("La anotación de historia clínica es obligatoria");
        }
        if (anotacionHistoriaRq.getCitaId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio");
        }
        if (anotacionHistoriaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (anotacionHistoriaRq.getDescripcion() == null || anotacionHistoriaRq.getDescripcion().isEmpty()) {
            throw new BadRequestException("La descripción de la anotación es obligatoria");
        }
        if (anotacionHistoriaRq.getDiagnostico() == null || anotacionHistoriaRq.getDiagnostico().isEmpty()) {
            throw new BadRequestException("El diagnóstico de la anotación es obligatorio");
        }
        if (anotacionHistoriaRq.getObservaciones() == null || anotacionHistoriaRq.getObservaciones().isEmpty()) {
            throw new BadRequestException("Las observaciones de la anotación son obligatorias");
        }
    }
=======
    @Override
    public List<AnotacionHistoria> listarAnotaciones(Long historiaId) throws BadRequestException {
        this.validarHistoriaExiste(historiaId);
        return anotacionRepository.findByHistoriaIdOrderByFechaHoraDesc(historiaId);
    }

    @Override
    public List<AnotacionHistoria> filtrarAnotaciones(Long historiaId, String fechaInicial, String fechaFinal) throws BadRequestException {
        this.validarHistoriaExiste(historiaId);
        LocalDateTime inicio = FechaUtil.parsearFecha(fechaInicial, false);
        LocalDateTime fin = FechaUtil.parsearFecha(fechaFinal, true);
        if (inicio.isAfter(fin)) {
            throw new BadRequestException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return anotacionRepository.findByHistoriaIdAndFechaHoraBetweenOrderByFechaHoraDesc(historiaId, inicio, fin);
    }

    @Override
    public AnotacionHistoria consultarAnotacion(Long id) throws BadRequestException {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID de la anotación no puede estar vacío ni ser menor o igual a cero");
        }
        return anotacionRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La anotación con ID " + id + " no existe", HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(anotacionRq);

        // Paso 2. Verifico la historia y el veterinario
        HistoriaMedica historia = historiaRepository.findById(anotacionRq.getHistoriaId())
                .orElseThrow(() -> new BadRequestException("La historia clínica con ID " + anotacionRq.getHistoriaId() + " no existe", HttpStatus.NOT_FOUND));
        Medico medico = medicoRepository.findById(anotacionRq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario con ID " + anotacionRq.getMedicoId() + " no existe", HttpStatus.NOT_FOUND));

        // Paso 3. Creo la anotación con la fecha y hora actuales
        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setHistoria(historia);
        anotacion.setMedico(medico);
        anotacion.setFechaHora(LocalDateTime.now());
        this.cargarDatos(anotacion, anotacionRq);
        this.anotacionRepository.save(anotacion);

        // Paso 4. Retorno la respuesta
        return respuesta("Anotación guardada correctamente");
    }

    @Override
    public MiRespuestaRS actualizarAnotacion(AnotacionHistoriaRq anotacionRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(anotacionRq);
        if (anotacionRq.getId() == null || anotacionRq.getId() <= 0) {
            throw new BadRequestException("El ID de la anotación es obligatorio para actualizar");
        }

        // Paso 2. Verifico que exista y que la historia y el veterinario no cambien
        AnotacionHistoria anotacion = this.consultarAnotacion(anotacionRq.getId());
        if (!anotacion.getHistoria().getId().equals(anotacionRq.getHistoriaId())) {
            throw new BadRequestException("No se puede cambiar la historia clínica de una anotación", HttpStatus.CONFLICT);
        }
        if (!anotacion.getMedico().getId().equals(anotacionRq.getMedicoId())) {
            throw new BadRequestException("No se puede cambiar el veterinario que registró la anotación", HttpStatus.CONFLICT);
        }

        // Paso 3. Actualizo el contenido
        this.cargarDatos(anotacion, anotacionRq);
        this.anotacionRepository.save(anotacion);

        // Paso 4. Retorno la respuesta
        return respuesta("Anotación actualizada correctamente");
    }

    private void validarHistoriaExiste(Long historiaId) {
        if (historiaId == null || historiaId <= 0) {
            throw new BadRequestException("El ID de la historia no puede estar vacío ni ser menor o igual a cero");
        }
        if (!historiaRepository.existsById(historiaId)) {
            throw new BadRequestException("La historia clínica con ID " + historiaId + " no existe", HttpStatus.NOT_FOUND);
        }
    }

    private void cargarDatos(AnotacionHistoria anotacion, AnotacionHistoriaRq rq) {
        anotacion.setDescripcion(rq.getDescripcion().trim());
        anotacion.setObservacion(rq.getObservacion().trim());
        anotacion.setDiagnosticoEvolucion(rq.getDiagnosticoEvolucion().trim());
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(AnotacionHistoriaRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (rq.getHistoriaId() == null || rq.getHistoriaId() <= 0) {
            throw new BadRequestException("El ID de la historia clínica es obligatorio");
        }
        if (rq.getMedicoId() == null || rq.getMedicoId() <= 0) {
            throw new BadRequestException("El ID del veterinario es obligatorio");
        }
        if (rq.getDescripcion() == null || rq.getDescripcion().isBlank()) {
            throw new BadRequestException("La descripción de la anotación es obligatoria");
        }
        if (rq.getObservacion() == null || rq.getObservacion().isBlank()) {
            throw new BadRequestException("La observación de la anotación es obligatoria");
        }
        if (rq.getDiagnosticoEvolucion() == null || rq.getDiagnosticoEvolucion().isBlank()) {
            throw new BadRequestException("El diagnóstico o evolución es obligatorio");
        }
    }

    /** {@inheritDoc} */
    @Override
    public PaginaRS<AnotacionVistaRS> listarAnotacionesPaginadas(Filtros.Anotacion filtros, int pagina, int tamanio,
                                                                 String campo, String direccion) throws BadRequestException {
        // Paso 1. La historia es obligatoria y debe existir
        if (filtros == null || filtros.historiaId() == null) {
            throw new BadRequestException("La historia clínica es obligatoria para listar sus anotaciones");
        }
        this.validarHistoriaExiste(filtros.historiaId());

        // Paso 2. Valido la paginación y consulto la página
        Pageable pageable = Paginacion.crear(pagina, tamanio, campo, direccion,
                Set.of("fechaHora", "descripcion"), "fechaHora");
        Page<AnotacionHistoria> page = anotacionRepository.findAll(Especificaciones.anotacion(filtros), pageable);

        // Paso 3. Convierto cada anotación en su vista
        return PaginaRS.desde(page, this::aVista);
    }

    private AnotacionVistaRS aVista(AnotacionHistoria a) {
        return AnotacionVistaRS.builder()
                .id(a.getId())
                .historiaId(a.getHistoria().getId())
                .medicoId(a.getMedico().getId())
                .medicoNombre((a.getMedico().getNombres() + " " + a.getMedico().getApellidos()).trim())
                .fechaHora(a.getFechaHora())
                .descripcion(a.getDescripcion())
                .observacion(a.getObservacion())
                .diagnosticoEvolucion(a.getDiagnosticoEvolucion())
                .build();
    }
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
