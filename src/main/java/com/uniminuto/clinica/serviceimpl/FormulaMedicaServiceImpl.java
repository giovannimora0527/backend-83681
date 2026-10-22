package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.FormulaVistaRS;
import com.uniminuto.clinica.models.Filtros;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.PaginaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.util.Especificaciones;
import com.uniminuto.clinica.util.Paginacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Implementación de la lógica de negocio de fórmulas médicas.
 * Reglas: la mascota existe, la cita existe y es de esa mascota, el veterinario existe,
 * la fecha de emisión no es anterior a la consulta, el medicamento existe (id_medicamento, obligatorio),
 * la historia clínica, si se indica (id_historia), pertenece a la misma cita y mascota,
 * y la dosis, frecuencia y duración son válidas.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /** Duración máxima razonable de un tratamiento, en días. Coincide con el CHECK de la base. */
    private static final int MAX_DURACION_DIAS = 365;

    /** Campos por los que el DataTable puede ordenar. */
    private static final Set<String> CAMPOS_ORDEN = Set.of(
            "fechaCreacionRegistro", "fechaEmision", "medicamento", "duracionDias", "frecuencia", "dosis",
            "mascotaId", "medicoId");

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Override
    public List<FormulaMedica> listarFormulasMedicas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    @Override
    public PaginaRS<FormulaVistaRS> listarFormulasPaginadas(Filtros.Formula filtros, int pagina, int tamanio,
                                                            String campo, String direccion) throws BadRequestException {
        Pageable pageable = Paginacion.crear(pagina, tamanio, campo, direccion,
                CAMPOS_ORDEN, "fechaCreacionRegistro");
        Page<FormulaMedica> page = formulaMedicaRepository.findAll(Especificaciones.formula(filtros), pageable);
        return PaginaRS.desde(page, this::aVista);
    }

    @Override
    public List<FormulaMedica> listarFormulasPorMascota(Integer mascotaId) throws BadRequestException {
        if (mascotaId == null || mascotaId <= 0) {
            throw new BadRequestException("El ID de la mascota no puede estar vacío ni ser menor o igual a cero");
        }
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new BadRequestException("La mascota con ID " + mascotaId + " no existe", HttpStatus.NOT_FOUND);
        }
        return formulaMedicaRepository.findByMascotaMascotaIdOrderByFechaCreacionRegistroDesc(mascotaId);
    }

    @Override
    public FormulaMedica consultarFormulaMedica(Long id) throws BadRequestException {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID de la fórmula no puede estar vacío ni ser menor o igual a cero");
        }
        return formulaMedicaRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("La fórmula médica con ID " + id + " no existe", HttpStatus.NOT_FOUND));
    }

    @Override
    public MiRespuestaRS guardarFormulaMedica(FormulaMedicaRq formulaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(formulaRq);

        // Paso 2. Verifico mascota y veterinario
        Mascota mascota = mascotaRepository.findById(formulaRq.getMascotaId())
                .orElseThrow(() -> new BadRequestException("La mascota con ID " + formulaRq.getMascotaId() + " no existe", HttpStatus.NOT_FOUND));
        Medico medico = medicoRepository.findById(formulaRq.getMedicoId())
                .orElseThrow(() -> new BadRequestException("El veterinario con ID " + formulaRq.getMedicoId() + " no existe", HttpStatus.NOT_FOUND));

        // Paso 3. Verifico que la cita exista y sea de la misma mascota
        Cita cita = this.obtenerCitaDeMascota(formulaRq.getCitaId(), mascota);

        // Paso 4. La fecha de emisión no puede ser anterior a la consulta
        this.validarFechaEmision(formulaRq.getFechaEmision(), cita);

        // Paso 5. Creo la fórmula
        FormulaMedica formula = new FormulaMedica();
        formula.setMascota(mascota);
        formula.setCita(cita);
        formula.setMedico(medico);
        this.cargarDatosClinicos(formula, formulaRq);
        this.formulaMedicaRepository.save(formula);

        // Paso 6. Retorno la respuesta
        return respuesta("Fórmula médica guardada correctamente");
    }

    @Override
    public MiRespuestaRS actualizarFormulaMedica(FormulaMedicaRq formulaRq) throws BadRequestException {
        // Paso 1. Valido el objeto de entrada
        this.validarObjetoEntrada(formulaRq);
        if (formulaRq.getId() == null || formulaRq.getId() <= 0) {
            throw new BadRequestException("El ID de la fórmula es obligatorio para actualizar");
        }

        // Paso 2. La mascota, la cita y el veterinario no pueden cambiar
        FormulaMedica formula = this.consultarFormulaMedica(formulaRq.getId());
        if (!formula.getMascota().getMascotaId().equals(formulaRq.getMascotaId())
                || !formula.getCita().getId().equals(formulaRq.getCitaId())
                || !formula.getMedico().getId().equals(formulaRq.getMedicoId())) {
            throw new BadRequestException("No se puede cambiar la mascota, la cita ni el veterinario de una fórmula",
                    HttpStatus.CONFLICT);
        }

        // Paso 3. Valido la fecha de emisión y actualizo los datos clínicos
        this.validarFechaEmision(formulaRq.getFechaEmision(), formula.getCita());
        this.cargarDatosClinicos(formula, formulaRq);
        this.formulaMedicaRepository.save(formula);

        // Paso 4. Retorno la respuesta
        return respuesta("Fórmula médica actualizada correctamente");
    }

    /**
     * Verifica que la cita exista y pertenezca a la mascota indicada.
     */
    private Cita obtenerCitaDeMascota(Long citaId, Mascota mascota) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new BadRequestException("La cita con ID " + citaId + " no existe", HttpStatus.NOT_FOUND));
        if (!cita.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La cita seleccionada pertenece a otra mascota", HttpStatus.CONFLICT);
        }
        return cita;
    }

    private void validarFechaEmision(LocalDate fechaEmision, Cita cita) {
        if (fechaEmision.isBefore(cita.getFechaHora().toLocalDate())) {
            throw new BadRequestException("La fecha de emisión no puede ser anterior a la fecha de la consulta asociada");
        }
    }

    /**
     * Resuelve el medicamento (obligatorio) y la historia clínica (opcional) y los asigna a la fórmula.
     * Debe llamarse cuando la mascota y la cita de la fórmula ya están asignadas.
     */
    private void cargarDatosClinicos(FormulaMedica formula, FormulaMedicaRq rq) {
        formula.setMedicamento(this.obtenerMedicamento(rq.getIdMedicamento()));
        formula.setHistoria(this.obtenerHistoriaDeCita(rq.getIdHistoria(), formula.getCita(), formula.getMascota()));
        formula.setDosis(rq.getDosis().trim());
        formula.setFrecuencia(rq.getFrecuencia().trim());
        formula.setDuracionDias(rq.getDuracionDias());
        formula.setIndicaciones(rq.getIndicaciones().trim());
        formula.setFechaEmision(rq.getFechaEmision());
    }

    private Medicamento obtenerMedicamento(Long idMedicamento) {
        return medicamentoRepository.findById(idMedicamento)
                .orElseThrow(() -> new BadRequestException("El medicamento con ID " + idMedicamento + " no existe", HttpStatus.NOT_FOUND));
    }

    /**
     * Devuelve la historia clínica indicada, o null si no se envió.
     * La historia debe ser de la misma cita y de la misma mascota.
     */
    private HistoriaMedica obtenerHistoriaDeCita(Long idHistoria, Cita cita, Mascota mascota) {
        if (idHistoria == null) {
            return null;
        }
        HistoriaMedica historia = historiaMedicaRepository.findById(idHistoria)
                .orElseThrow(() -> new BadRequestException("La historia clínica con ID " + idHistoria + " no existe", HttpStatus.NOT_FOUND));
        if (!historia.getCita().getId().equals(cita.getId())
                || !historia.getMascota().getMascotaId().equals(mascota.getMascotaId())) {
            throw new BadRequestException("La historia clínica no corresponde a la misma cita y mascota de la fórmula", HttpStatus.CONFLICT);
        }
        return historia;
    }

    private FormulaVistaRS aVista(FormulaMedica f) {
        return FormulaVistaRS.builder()
                .id(f.getId())
                .mascotaId(f.getMascota().getMascotaId().longValue())
                .mascotaNombre(f.getMascota().getNombreMascota())
                .citaId(f.getCita().getId())
                .medicoId(f.getMedico().getId())
                .medicoNombre((f.getMedico().getNombres() + " " + f.getMedico().getApellidos()).trim())
                .idMedicamento(f.getMedicamento().getId())
                .medicamento(f.getMedicamento().getNombre())
                .idHistoria(f.getHistoria() != null ? f.getHistoria().getId() : null)
                .dosis(f.getDosis())
                .frecuencia(f.getFrecuencia())
                .duracionDias(f.getDuracionDias())
                .indicaciones(f.getIndicaciones())
                .fechaEmision(f.getFechaEmision())
                .fechaCreacionRegistro(f.getFechaCreacionRegistro())
                .fechaActualizacionRegistro(f.getFechaActualizacionRegistro())
                .build();
    }

    private MiRespuestaRS respuesta(String mensaje) {
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage(mensaje);
        return respuesta;
    }

    private void validarObjetoEntrada(FormulaMedicaRq rq) throws BadRequestException {
        if (rq == null) {
            throw new BadRequestException("El objeto de entrada no puede estar vacío");
        }
        if (rq.getMascotaId() == null || rq.getMascotaId() <= 0) {
            throw new BadRequestException("La mascota de la fórmula es obligatoria");
        }
        if (rq.getCitaId() == null || rq.getCitaId() <= 0) {
            throw new BadRequestException("La fórmula debe asociarse a una consulta (cita)");
        }
        if (rq.getMedicoId() == null || rq.getMedicoId() <= 0) {
            throw new BadRequestException("El veterinario de la fórmula es obligatorio");
        }
        if (rq.getIdMedicamento() == null || rq.getIdMedicamento() <= 0) {
            throw new BadRequestException("El medicamento de la fórmula es obligatorio");
        }
        if (rq.getIdHistoria() != null && rq.getIdHistoria() <= 0) {
            throw new BadRequestException("El ID de la historia clínica no es válido");
        }
        if (rq.getDosis() == null || rq.getDosis().isBlank()) {
            throw new BadRequestException("La dosis es obligatoria");
        }
        if (rq.getDosis().trim().length() > 100) {
            throw new BadRequestException("La dosis no puede superar 100 caracteres");
        }
        if (rq.getFrecuencia() == null || rq.getFrecuencia().isBlank()) {
            throw new BadRequestException("La frecuencia es obligatoria");
        }
        if (rq.getFrecuencia().trim().length() > 100) {
            throw new BadRequestException("La frecuencia no puede superar 100 caracteres");
        }
        if (rq.getDuracionDias() == null || rq.getDuracionDias() <= 0 || rq.getDuracionDias() > MAX_DURACION_DIAS) {
            throw new BadRequestException("La duración del tratamiento debe estar entre 1 y " + MAX_DURACION_DIAS + " días");
        }
        if (rq.getIndicaciones() == null || rq.getIndicaciones().isBlank()) {
            throw new BadRequestException("Las indicaciones son obligatorias");
        }
        if (rq.getFechaEmision() == null) {
            throw new BadRequestException("La fecha de emisión es obligatoria");
        }
    }
}
