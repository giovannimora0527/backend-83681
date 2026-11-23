package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.repository.MovimientoInventarioRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.entity.LoteMedicamento;
import com.uniminuto.clinica.entity.MovimientoInventario;
import com.uniminuto.clinica.entity.TipoMovimientoInventario;
import com.uniminuto.clinica.entity.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.uniminuto.clinica.entity.DespachoInventario;
import com.uniminuto.clinica.models.DespacharFormulaRq;
import com.uniminuto.clinica.models.DespachoRs;
import com.uniminuto.clinica.repository.DespachoInventarioRepository;
import com.uniminuto.clinica.util.PdfUtil;

@Service
public class FormulaServiceImpl implements FormulaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired private LoteMedicamentoRepository loteRepository;
    @Autowired private MovimientoInventarioRepository movimientoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DespachoInventarioRepository despachoRepository;
    @Autowired private FacturacionService facturacionService;

    @Override
    public List<FormulaMedica> findAllFormulas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    @Override
    @Transactional
    public MiRespuestaRS guardarFormula(FormulaRq formulaRq) throws BadRequestException {
        this.validarFormula(formulaRq);
        Cita cita = this.existeCita(formulaRq);
        Medicamento medicamento = this.existeMedicamento(formulaRq);
        FormulaMedica formulaNueva = new FormulaMedica();
        formulaNueva.setCita(cita);
        formulaNueva.setMedicamento(medicamento);
        formulaNueva.setDosis(formulaRq.getDosis());
        formulaNueva.setIndicaciones(formulaRq.getIndicaciones());
        formulaNueva.setDuracion(formulaRq.getDuracion());
        formulaNueva.setFrecuencia(formulaRq.getFrecuencia());
        formulaNueva.setCantidadPrescrita(formulaRq.getCantidadPrescrita() == null ? 1 : formulaRq.getCantidadPrescrita());
        formulaNueva.setEstadoDispensacion("PENDIENTE");
        formulaNueva.setViaAdministracion(validarVia(formulaRq.getViaAdministracion()));
        formulaNueva.setFechaCreacionRegistro(LocalDateTime.now());
        formulaMedicaRepository.save(formulaNueva);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Fórmula médica guardada correctamente.");
        return rta;
    }

    private Cita existeCita(FormulaRq formulaRq) throws BadRequestException {
        Optional<Cita> citaOptional = citaRepository.findById(formulaRq.getCitaId());
        if (citaOptional.isEmpty()) {
            throw new BadRequestException("La cita con el id proporcionado no existe.");
        }
        return citaOptional.get();
    }

    private Medicamento existeMedicamento(FormulaRq formulaRq) throws BadRequestException {
        Optional<Medicamento> medicamentoOptional = medicamentoRepository.findById(formulaRq.getMedicamentoId());
        if (medicamentoOptional.isEmpty()) {
            throw new BadRequestException("El medicamento con el id proporcionado no existe.");
        }
        return medicamentoOptional.get();
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarFormula(FormulaRq formulaRq) throws BadRequestException {
        this.validarFormula(formulaRq);
        Cita cita = this.existeCita(formulaRq);
        Medicamento medicamento = this.existeMedicamento(formulaRq);
        Optional<FormulaMedica> optionalFormulaMedica = this.formulaMedicaRepository.findById(formulaRq.getId());
        if (optionalFormulaMedica.isEmpty()) {
            throw new BadRequestException("La fórmula médica con el id proporcionado no existe.");
        }
        FormulaMedica formulaExistente = optionalFormulaMedica.get();
        if (!"PENDIENTE".equals(formulaExistente.getEstadoDispensacion())) {
            throw new BadRequestException("No se puede modificar una fórmula que ya tiene despacho.");
        }
        formulaExistente.setCita(cita);
        formulaExistente.setMedicamento(medicamento);
        formulaExistente.setDosis(formulaRq.getDosis());
        formulaExistente.setIndicaciones(formulaRq.getIndicaciones());
        formulaExistente.setDuracion(formulaRq.getDuracion());
        formulaExistente.setFrecuencia(formulaRq.getFrecuencia());
        formulaExistente.setCantidadPrescrita(formulaRq.getCantidadPrescrita() == null ? 1 : formulaRq.getCantidadPrescrita());
        formulaExistente.setViaAdministracion(validarVia(formulaRq.getViaAdministracion()));
        formulaExistente.setFechaActualizacionRegistro(LocalDateTime.now());
        this.formulaMedicaRepository.save(formulaExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Fórmula médica actualizada correctamente.");
        return rta;
    }

    @Override
    @Transactional
    public MiRespuestaRS dispensar(Long formulaId) throws BadRequestException {
        FormulaMedica formula = formulaMedicaRepository.findByIdForUpdate(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula solicitada no existe."));
        if (!"PENDIENTE".equals(formula.getEstadoDispensacion())) {
            throw new BadRequestException("La fórmula ya fue despachada; no se puede descontar dos veces.");
        }
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = auth == null ? null : usuarioRepository.findByUsername(auth.getName()).orElse(null);
        if (usuario == null) throw new BadRequestException("No se encontró el usuario autenticado.");
        int pendiente = formula.getCantidadPrescrita();
        var lotes = loteRepository.findDispensablesFefoForUpdate(formula.getMedicamento().getId(), java.time.LocalDate.now());
        int disponible = lotes.stream().mapToInt(LoteMedicamento::getCantidadDisponible).sum();
        if (disponible < pendiente) throw new BadRequestException("Inventario dispensable insuficiente; no se realizaron descuentos.");
        var ahora = LocalDateTime.now();
        for (LoteMedicamento lote : lotes) {
            if (pendiente == 0) break;
            int cantidad = Math.min(pendiente, lote.getCantidadDisponible());
            int anterior = lote.getCantidadDisponible();
            lote.setCantidadDisponible(anterior - cantidad);
            loteRepository.save(lote);
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setLote(lote); movimiento.setTipo(TipoMovimientoInventario.DESPACHO);
            movimiento.setCantidad(cantidad); movimiento.setStockAnterior(anterior);
            movimiento.setStockNuevo(anterior - cantidad); movimiento.setUsuario(usuario);
            movimiento.setFormula(formula); movimiento.setReferencia("FORMULA-" + formula.getId());
            movimiento.setDetalle("Despacho FEFO de fórmula médica"); movimiento.setFecha(ahora);
            movimientoRepository.save(movimiento);
            pendiente -= cantidad;
        }
        formula.setEstadoDispensacion("DESPACHADA");
        formula.setFechaActualizacionRegistro(ahora);
        formulaMedicaRepository.save(formula);
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200); respuesta.setMessage("Fórmula despachada; movimientos de inventario registrados.");
        return respuesta;
    }

    @Override
    @Transactional
    public DespachoRs dispensarParcial(Long formulaId, DespacharFormulaRq request) {
        if (request == null || request.getCantidad() == null || request.getCantidad() <= 0
                || request.getClaveIdempotencia() == null
                || !request.getClaveIdempotencia().matches("[A-Za-z0-9._:-]{8,64}")) {
            throw new BadRequestException("Indica cantidad positiva y clave de idempotencia de 8 a 64 caracteres.");
        }
        FormulaMedica formula = formulaMedicaRepository.findByIdForUpdate(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula solicitada no existe."));
        Optional<DespachoInventario> existente = despachoRepository.findByClaveIdempotencia(request.getClaveIdempotencia());
        if (existente.isPresent()) {
            DespachoInventario previous = existente.get();
            if (!previous.getFormula().getId().equals(formulaId)
                    || !previous.getCantidadSolicitada().equals(request.getCantidad()))
                throw new BadRequestException("La clave ya se utilizó para una solicitud diferente.");
            return construirRespuestaDespacho(previous, formula, true);
        }
        if (!List.of("PENDIENTE", "PARCIAL").contains(formula.getEstadoDispensacion()))
            throw new BadRequestException("La fórmula ya está completada.");

        List<MovimientoInventario> movimientosPrevios = movimientoRepository.findByFormula_IdOrderByFechaAscIdAsc(formulaId);
        int despachadoPrevio = movimientosPrevios.stream().filter(m -> m.getTipo() == TipoMovimientoInventario.DESPACHO)
                .mapToInt(MovimientoInventario::getCantidad).sum();
        int restante = formula.getCantidadPrescrita() - despachadoPrevio;
        if (request.getCantidad() > restante)
            throw new BadRequestException("La cantidad excede las " + restante + " unidades pendientes.");

        var auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = auth == null ? null : usuarioRepository.findByUsername(auth.getName()).orElse(null);
        if (usuario == null) throw new BadRequestException("No se encontró el usuario autenticado.");
        List<LoteMedicamento> lotes = loteRepository.findDispensablesFefoForUpdate(
                formula.getMedicamento().getId(), java.time.LocalDate.now());
        int cantidadReal = Math.min(request.getCantidad(), lotes.stream().mapToInt(LoteMedicamento::getCantidadDisponible).sum());
        if (cantidadReal == 0) throw new BadRequestException("No hay existencias vigentes para despachar.");

        LocalDateTime ahora = LocalDateTime.now();
        DespachoInventario registro = new DespachoInventario();
        registro.setFormula(formula); registro.setUsuario(usuario);
        registro.setClaveIdempotencia(request.getClaveIdempotencia());
        registro.setCantidadSolicitada(request.getCantidad()); registro.setCantidadDespachada(cantidadReal);
        registro.setFecha(ahora); registro = despachoRepository.saveAndFlush(registro);
        int porDespachar = cantidadReal;
        for (LoteMedicamento lote : lotes) {
            if (porDespachar == 0) break;
            int cantidad = Math.min(porDespachar, lote.getCantidadDisponible());
            int stockAnterior = lote.getCantidadDisponible();
            lote.setCantidadDisponible(stockAnterior - cantidad); loteRepository.save(lote);
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setLote(lote); movimiento.setTipo(TipoMovimientoInventario.DESPACHO);
            movimiento.setCantidad(cantidad); movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockNuevo(stockAnterior - cantidad); movimiento.setUsuario(usuario);
            movimiento.setFormula(formula); movimiento.setDespacho(registro);
            movimiento.setReferencia("FORMULA-" + formulaId); movimiento.setDetalle("Despacho FEFO parcial");
            movimiento.setFecha(ahora); movimientoRepository.save(movimiento);
            porDespachar -= cantidad;
        }
        int totalDespachado = despachadoPrevio + cantidadReal;
        formula.setEstadoDispensacion(totalDespachado >= formula.getCantidadPrescrita() ? "DESPACHADA" : "PARCIAL");
        formula.setFechaActualizacionRegistro(ahora); formulaMedicaRepository.save(formula);
        facturacionService.registrarDespachoEnFactura(registro.getId());
        return construirRespuestaDespacho(registro, formula, false);
    }

    private DespachoRs construirRespuestaDespacho(DespachoInventario registro, FormulaMedica formula, boolean repetido) {
        int totalDespachado = movimientoRepository.findByFormula_IdOrderByFechaAscIdAsc(formula.getId()).stream()
                .filter(m -> m.getTipo() == TipoMovimientoInventario.DESPACHO)
                .mapToInt(MovimientoInventario::getCantidad).sum();
        DespachoRs result = new DespachoRs(); result.setDespachoId(registro.getId());
        result.setFormulaId(formula.getId()); result.setCantidadSolicitada(registro.getCantidadSolicitada());
        result.setCantidadDespachada(registro.getCantidadDespachada());
        result.setCantidadPendiente(Math.max(0, formula.getCantidadPrescrita() - totalDespachado));
        result.setEstadoFormula(formula.getEstadoDispensacion()); result.setClaveIdempotencia(registro.getClaveIdempotencia());
        result.setFecha(registro.getFecha()); result.setRepetido(repetido); return result;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdf(Long formulaId) {
        FormulaMedica f = formulaMedicaRepository.findById(formulaId)
                .orElseThrow(() -> new BadRequestException("La fórmula solicitada no existe."));
        var cita = f.getCita(); var mascota = cita.getMascota(); var medico = cita.getMedico();
        return PdfUtil.generar("Formula medica #" + f.getId(), List.of(
                "Fecha: " + f.getFechaCreacionRegistro(), "Paciente: " + mascota.getNombreMascota(),
                "Propietario: " + mascota.getCliente().getNombres() + " " + mascota.getCliente().getApellidos(),
                "Profesional: " + medico.getNombres() + " " + medico.getApellidos(),
                "Medicamento: " + f.getMedicamento().getNombre(), "Dosis: " + f.getDosis(),
                "Via: " + f.getViaAdministracion(), "Frecuencia: " + f.getFrecuencia(),
                "Duracion: " + f.getDuracion(), "Cantidad prescrita: " + f.getCantidadPrescrita(),
                "Estado de despacho: " + f.getEstadoDispensacion(), "Indicaciones: " + f.getIndicaciones()));
    }


    private void validarFormula(FormulaRq formulaRq) throws BadRequestException {
        if (formulaRq.getMedicamentoId() == null || formulaRq.getMedicamentoId() <= 0) {
            throw new BadRequestException("El id del medicamento es obligatorio y debe ser mayor a cero.");
        }
        if (formulaRq.getCantidadPrescrita() != null && formulaRq.getCantidadPrescrita() <= 0) {
            throw new BadRequestException("La cantidad prescrita debe ser mayor a cero.");
        }
        if (formulaRq.getCitaId() == null || formulaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita es obligatorio y debe ser mayor a cero.");
        }
        if (formulaRq.getDosis() == null || formulaRq.getDosis().isEmpty()) {
            throw new BadRequestException("La dosis de la fórmula es obligatoria.");
        }
        if (formulaRq.getIndicaciones() == null || formulaRq.getIndicaciones().isEmpty()) {
            throw new BadRequestException("Las indicaciones de la fórmula son obligatorias.");
        }
        if(formulaRq.getDuracion() == null || formulaRq.getDuracion().isEmpty()) {
            throw new BadRequestException("La duración de la fórmula es obligatoria.");
        }
        if(formulaRq.getFrecuencia() == null || formulaRq.getFrecuencia().isEmpty()) {
            throw new BadRequestException("La frecuencia de la fórmula es obligatoria.");
        }

    }

    private String validarVia(String via) {
        String value = via == null || via.isBlank() ? "ORAL" : via.trim().toUpperCase(java.util.Locale.ROOT);
        if (!List.of("ORAL", "TOPICA", "INYECTABLE", "OFTALMICA", "OTICA", "RECTAL", "OTRA").contains(value))
            throw new BadRequestException("Vía no válida. Opciones: ORAL, TOPICA, INYECTABLE, OFTALMICA, OTICA, RECTAL u OTRA.");
        return value;
    }
}
