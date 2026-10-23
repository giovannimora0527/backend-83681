package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.CitaMedica;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import com.uniminuto.clinica.util.EstadoCita;
import com.uniminuto.clinica.util.RespuestaUtil;
import com.uniminuto.clinica.util.ValidadorEntidades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Logica de negocio de las formulas medicas.
 *
 * Reglas implementadas (numeradas como en el enunciado del parcial):
 * 1. No se crea una formula para una mascota inexistente.
 * 2. La mascota debe pertenecer a un cliente registrado (y al cliente seleccionado).
 * 3. El veterinario debe existir.
 * 4. La fecha de emision no puede ser anterior a la fecha de la cita asociada.
 * 5. La dosis (mayor que 0) y la duracion (1 a 365 dias) deben ser valores validos.
 * 6. Los campos obligatorios se validan aqui (backend) y tambien en Angular.
 * 7. La cita asociada debe ser de la misma mascota.
 * 8. No hay borrado fisico: la formula se inactiva.
 * Adicional: solo se emiten formulas en citas ATENDIDAS.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    /**
     * Duracion maxima permitida para un tratamiento (en dias).
     */
    private static final int DURACION_MAXIMA_DIAS = 365;

    /**
     * Unidades de dosis permitidas.
     */
    private static final List<String> UNIDADES_DOSIS = List.of("mg", "g", "ml", "mg/kg", "tableta", "gotas", "UI");

    /**
     * Repositorio de formulas.
     */
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    /**
     * Validaciones comunes (cliente, mascota, veterinario, cita).
     */
    @Autowired
    private ValidadorEntidades validador;

    @Override
    public List<FormulaMedica> listarFormulas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public List<FormulaMedica> listarFormulasOrdenadas(boolean asc) {
        return asc ? formulaMedicaRepository.findAllByOrderByFechaCreacionAsc()
                : formulaMedicaRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Override
    public FormulaMedica consultarFormula(Long formulaId) throws BadRequestException {
        validador.validarIdObligatorio(formulaId, "fórmula");
        return formulaMedicaRepository.findById(formulaId)
                .orElseThrow(() -> new NotFoundException("La fórmula médica con ID " + formulaId + " no existe."));
    }

    @Override
    public MiRespuestaRS crearFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        // Paso 1. Regla 5 y 6: campos obligatorios y valores validos
        this.validarDatosFormula(formulaRq);

        // Paso 2. Reglas 1 y 2: la mascota existe y es del cliente
        Mascota mascota = validador.obtenerMascotaDelCliente(formulaRq.getClienteId(), formulaRq.getMascotaId());

        // Paso 3. Regla 3: el veterinario existe
        Medico medico = validador.obtenerMedico(formulaRq.getMedicoId());

        // Paso 4. Regla 7: la cita existe y es de la misma mascota
        CitaMedica cita = validador.obtenerCita(formulaRq.getCitaId());
        validador.validarCitaDeLaMascota(cita, mascota);

        // Paso 5. La formula se emite en una consulta que ya ocurrio
        if (!EstadoCita.ATENDIDA.equals(cita.getEstado())) {
            throw new ConflictException("Solo se pueden emitir fórmulas para citas ATENDIDAS. "
                    + "Estado actual de la cita: " + cita.getEstado() + ".");
        }

        // Paso 6. Regla 4: fecha de emision coherente con la cita
        this.validarFechaEmision(formulaRq.getFechaEmision(), cita);

        // Paso 7. Guardar (activa por defecto)
        FormulaMedica formula = new FormulaMedica();
        formula.setMascota(mascota);
        formula.setMedico(medico);
        formula.setCita(cita);
        this.asignarDatosEditables(formula, formulaRq);
        formula.setFechaCreacion(LocalDateTime.now());
        formula.setActivo(true);
        formulaMedicaRepository.save(formula);

        return RespuestaUtil.crear(201, "Fórmula médica creada correctamente para "
                + mascota.getNombreMascota() + ".");
    }

    @Override
    public MiRespuestaRS actualizarFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        // Paso 1. La formula existe y esta activa
        if (formulaRq == null || formulaRq.getId() == null) {
            throw new BadRequestException("El id de la fórmula es obligatorio para actualizar.");
        }
        FormulaMedica formula = this.consultarFormula(formulaRq.getId());
        if (!Boolean.TRUE.equals(formula.getActivo())) {
            throw new ConflictException("La fórmula está INACTIVA y no puede modificarse.");
        }

        // Paso 2. La mascota, la cita y el veterinario NO se cambian: son la
        // informacion clinica de origen de la formula.
        if (formulaRq.getMascotaId() != null && !formulaRq.getMascotaId().equals(formula.getMascota().getMascotaId())
                || formulaRq.getCitaId() != null && !formulaRq.getCitaId().equals(formula.getCita().getId())
                || formulaRq.getMedicoId() != null && !formulaRq.getMedicoId().equals(formula.getMedico().getId())) {
            throw new BadRequestException("No se puede cambiar la mascota, la cita ni el veterinario de una "
                    + "fórmula emitida. Inactívela y cree una nueva.");
        }

        // Paso 3. Validar los datos editables
        this.validarDatosFormula(formulaRq);
        this.validarFechaEmision(formulaRq.getFechaEmision(), formula.getCita());

        // Paso 4. Guardar cambios
        this.asignarDatosEditables(formula, formulaRq);
        formula.setFechaModificacion(LocalDateTime.now());
        formulaMedicaRepository.save(formula);

        return RespuestaUtil.crear(200, "Fórmula médica actualizada correctamente.");
    }

    @Override
    public MiRespuestaRS inactivarFormula(Long formulaId) throws BadRequestException {
        FormulaMedica formula = this.consultarFormula(formulaId);
        if (!Boolean.TRUE.equals(formula.getActivo())) {
            throw new ConflictException("La fórmula ya se encuentra inactiva.");
        }
        formula.setActivo(false);
        formula.setFechaModificacion(LocalDateTime.now());
        formulaMedicaRepository.save(formula);
        return RespuestaUtil.crear(200, "Fórmula médica inactivada correctamente.");
    }

    /**
     * Reglas 5 y 6: valida campos obligatorios y que la dosis y la duracion sean validas.
     *
     * @param formulaRq datos recibidos.
     * @throws BadRequestException si algun dato falta o es invalido.
     */
    private void validarDatosFormula(FormulaMedicaRq formulaRq) {
        if (formulaRq == null) {
            throw new BadRequestException("Los datos de la fórmula son obligatorios.");
        }
        validador.validarTextoObligatorio(formulaRq.getMedicamento(), "medicamento");
        validador.validarTextoObligatorio(formulaRq.getFrecuencia(), "frecuencia");
        validador.validarTextoObligatorio(formulaRq.getIndicaciones(), "indicaciones");
        if (formulaRq.getFechaEmision() == null) {
            throw new BadRequestException("El campo fecha de emisión es obligatorio.");
        }
        if (formulaRq.getDosisCantidad() == null || formulaRq.getDosisCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La dosis debe ser un número mayor que cero.");
        }
        if (formulaRq.getDosisUnidad() == null || !UNIDADES_DOSIS.contains(formulaRq.getDosisUnidad())) {
            throw new BadRequestException("La unidad de la dosis no es válida. Unidades permitidas: "
                    + String.join(", ", UNIDADES_DOSIS) + ".");
        }
        if (formulaRq.getDuracionDias() == null || formulaRq.getDuracionDias() < 1
                || formulaRq.getDuracionDias() > DURACION_MAXIMA_DIAS) {
            throw new BadRequestException("La duración del tratamiento debe estar entre 1 y "
                    + DURACION_MAXIMA_DIAS + " días.");
        }
    }

    /**
     * Regla 4: la fecha de emision no puede ser anterior a la cita, ni futura.
     *
     * @param fechaEmision fecha de emision recibida.
     * @param cita cita asociada a la formula.
     * @throws BadRequestException si la fecha no es coherente.
     */
    private void validarFechaEmision(LocalDateTime fechaEmision, CitaMedica cita) {
        if (fechaEmision.isBefore(cita.getFechaHora())) {
            throw new BadRequestException("La fecha de emisión no puede ser anterior a la fecha de la consulta ("
                    + cita.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ").");
        }
        if (fechaEmision.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de emisión no puede ser una fecha futura.");
        }
    }

    /**
     * Copia a la entidad los campos que se pueden crear o editar.
     *
     * @param formula entidad destino.
     * @param formulaRq datos recibidos.
     */
    private void asignarDatosEditables(FormulaMedica formula, FormulaMedicaRq formulaRq) {
        formula.setMedicamento(formulaRq.getMedicamento().trim());
        formula.setDosisCantidad(formulaRq.getDosisCantidad());
        formula.setDosisUnidad(formulaRq.getDosisUnidad());
        formula.setFrecuencia(formulaRq.getFrecuencia().trim());
        formula.setDuracionDias(formulaRq.getDuracionDias());
        formula.setIndicaciones(formulaRq.getIndicaciones().trim());
        formula.setFechaEmision(formulaRq.getFechaEmision());
    }
}
