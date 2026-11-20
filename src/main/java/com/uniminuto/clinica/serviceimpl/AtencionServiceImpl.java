package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AtencionRq;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.AtencionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AtencionServiceImpl implements AtencionService {
    private final AtencionMedicaRepository atenciones;
    private final CitaRepository citas;
    private final MedicoRepository medicos;
    private final MedicamentoRepository medicamentos;
    private final InventarioLoteRepository lotes;
    private final FormulaDetalleRepository formulas;
    private final CitaHistorialRepository historial;
    private final MedicoUsuarioRepository medicosUsuarios;
    private final MascotaRepository mascotas;
    private final FormulaMedicaRepository formulasPrevias;

    public AtencionServiceImpl(AtencionMedicaRepository atenciones, CitaRepository citas, MedicoRepository medicos,
                               MedicamentoRepository medicamentos, InventarioLoteRepository lotes,
                               FormulaDetalleRepository formulas, CitaHistorialRepository historial,
                               MedicoUsuarioRepository medicosUsuarios, MascotaRepository mascotas, FormulaMedicaRepository formulasPrevias) {
        this.atenciones = atenciones; this.citas = citas; this.medicos = medicos; this.medicamentos = medicamentos;
        this.lotes = lotes; this.formulas = formulas; this.historial = historial; this.medicosUsuarios = medicosUsuarios; this.mascotas = mascotas; this.formulasPrevias=formulasPrevias;
    }

    @Override @Transactional
    public AtencionMedica finalizar(AtencionRq rq, Long usuarioId, String role) {
        if (rq == null || rq.citaId() == null || rq.medicoId() == null || rq.motivo() == null || rq.motivo().isBlank())
            throw new BadRequestException("Cita, médico y motivo de consulta son obligatorios.");
        if ("MEDICO".equals(role)) {
            Long assigned = medicosUsuarios.findByUsuarioId(usuarioId).map(MedicoUsuario::getMedicoId)
                    .orElseThrow(() -> new BadRequestException("El usuario no tiene un médico asociado."));
            if (!assigned.equals(rq.medicoId())) throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "El médico solo puede atender sus propias citas.");
        }
        Cita cita = citas.findById(rq.citaId()).orElseThrow(() -> new BadRequestException("La cita no existe."));
        Medico medico = medicos.findById(rq.medicoId()).orElseThrow(() -> new BadRequestException("El médico no existe."));
        if (!cita.getMedico().getId().equals(medico.getId()))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "La cita pertenece a otro médico.");
        if ("CANCELADA".equalsIgnoreCase(cita.getEstado()) || "ATENDIDA".equalsIgnoreCase(cita.getEstado()))
            throw new BadRequestException("La cita cancelada o atendida no se puede finalizar de nuevo.");
        if (atenciones.findByCitaId(cita.getId()).isPresent()) throw new BadRequestException("La cita ya tiene una atención registrada.");

        AtencionMedica atencion = new AtencionMedica();
        atencion.setCitaId(cita.getId()); atencion.setMascotaId(cita.getMascota().getMascotaId()); atencion.setMedicoId(medico.getId());
        atencion.setMotivo(rq.motivo()); atencion.setSintomas(rq.sintomas()); atencion.setDiagnostico(rq.diagnostico());
        atencion.setObservaciones(rq.observaciones()); atencion.setRecomendaciones(rq.recomendaciones()); atencion.setAtendidaEn(LocalDateTime.now());
        atencion = atenciones.saveAndFlush(atencion);
        if (rq.formulas() != null) for (AtencionRq.Formula f : rq.formulas()) guardarFormula(atencion, f);

        String anterior = cita.getEstado(); cita.setEstado("ATENDIDA"); cita.setFechaActualizacion(LocalDateTime.now()); citas.save(cita);
        CitaHistorial evento = new CitaHistorial(); evento.setCitaId(cita.getId()); evento.setEstadoAnterior(anterior);
        evento.setEstadoNuevo("ATENDIDA"); evento.setNota("Atención médica finalizada"); evento.setUsuarioId(usuarioId);
        evento.setCreadoEn(LocalDateTime.now()); historial.save(evento);
        return atencion;
    }

    private void guardarFormula(AtencionMedica atencion, AtencionRq.Formula f) {
        if (f == null || f.medicamentoId() == null || f.cantidad() == null || f.cantidad().signum() <= 0
                || f.dosis() == null || f.dosis().isBlank()) throw new BadRequestException("Cada fórmula requiere producto, dosis y cantidad positiva.");
        Medicamento medicamento = medicamentos.findById(f.medicamentoId()).orElseThrow(() -> new BadRequestException("El medicamento no existe."));
        BigDecimal disponible = lotes.findByMedicamentoIdOrderByFechaVencimientoAsc(medicamento.getId()).stream()
                .filter(l -> !l.getFechaVencimiento().isBefore(LocalDate.now()))
                .map(InventarioLote::getCantidadDisponible).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (f.cantidad().compareTo(disponible) > 0) throw new BadRequestException("La cantidad formulada supera las existencias vigentes disponibles.");
        FormulaDetalle item = new FormulaDetalle(); item.setAtencionId(atencion.getId()); item.setMedicamento(medicamento);
        item.setCantidadPrescrita(f.cantidad()); item.setCantidadDispensada(BigDecimal.ZERO); item.setDosis(f.dosis());
        item.setFrecuencia(f.frecuencia()); item.setDuracion(f.duracion()); item.setViaAdministracion(f.viaAdministracion());
        item.setIndicaciones(f.indicaciones()); formulas.save(item);
    }

    @Override @Transactional(readOnly=true)
    public Object contexto(Integer mascotaId, Long usuarioId, String role) {
        Mascota mascota=mascotas.findById(mascotaId).orElseThrow(()->new BadRequestException("La mascota no existe."));
        var agenda=citas.findByMascotaOrderByFechaHoraDesc(mascota);
        if("MEDICO".equals(role)) {
            Long medicoId=medicosUsuarios.findByUsuarioId(usuarioId).map(MedicoUsuario::getMedicoId).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"El usuario no está asociado a un médico."));
            if(agenda.stream().noneMatch(c->c.getMedico().getId().equals(medicoId))) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"No tiene autorización para consultar la historia de esta mascota.");
        }
        List<Map<String,Object>> eventos=new java.util.ArrayList<>();
        for(Cita cita:agenda) eventos.add(Map.of("cita",cita,"historial",historial.findByCitaIdOrderByCreadoEnAsc(cita.getId()),"formulasPrevias",formulasPrevias.findByCita_IdInOrderByFechaCreacionRegistroDesc(List.of(cita.getId()))));
        List<Map<String,Object>> historialClinico=new java.util.ArrayList<>();
        for(AtencionMedica a:atenciones.findByMascotaIdOrderByAtendidaEnDesc(mascotaId)) historialClinico.add(Map.of("atencion",a,"formulas",formulas.findByAtencionIdOrderByIdAsc(a.getId())));
        return Map.of("mascota",mascota,"propietario",mascota.getCliente()==null?Map.of():mascota.getCliente(),"citas",eventos,"atenciones",historialClinico);
    }

    @Override @Transactional(readOnly=true)
    public byte[] pdf(Long atencionId, Long usuarioId, String role) {
        AtencionMedica a=atenciones.findById(atencionId).orElseThrow(()->new BadRequestException("La atención no existe."));
        Cita cita=citas.findById(a.getCitaId()).orElseThrow(()->new BadRequestException("La cita no existe."));
        if("MEDICO".equals(role)) {
            Long medicoId=medicosUsuarios.findByUsuarioId(usuarioId).map(MedicoUsuario::getMedicoId).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"El usuario no está asociado a un médico."));
            if(!medicoId.equals(a.getMedicoId())) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Solo puede generar documentos de sus atenciones.");
        }
        List<String> lineas=new java.util.ArrayList<>(); lineas.add("Mascota: "+cita.getMascota().getNombreMascota());
        lineas.add("Propietario: "+cita.getCliente().getNombres()+" "+cita.getCliente().getApellidos());
        lineas.add("Veterinario: "+cita.getMedico().getNombres()+" "+cita.getMedico().getApellidos());lineas.add("Fecha: "+a.getAtendidaEn());
        lineas.add("Motivo: "+a.getMotivo());lineas.add("Síntomas: "+a.getSintomas());lineas.add("Diagnóstico: "+a.getDiagnostico());
        lineas.add("Observaciones: "+a.getObservaciones());lineas.add("Recomendaciones: "+a.getRecomendaciones());lineas.add("Fórmulas:");
        for(FormulaDetalle f:formulas.findByAtencionIdOrderByIdAsc(atencionId)) lineas.add(f.getMedicamento().getNombre()+" | Cantidad: "+f.getCantidadPrescrita()+" | Dosis: "+f.getDosis()+" | Frecuencia: "+f.getFrecuencia()+" | Duración: "+f.getDuracion()+" | Vía: "+f.getViaAdministracion()+" | "+f.getIndicaciones());
        for(FormulaMedica f:formulasPrevias.findByCita_IdInOrderByFechaCreacionRegistroDesc(List.of(cita.getId()))) lineas.add("Fórmula previa: "+f.getMedicamento().getNombre()+" | "+f.getDosis()+" | "+f.getFrecuencia()+" | "+f.getDuracion());
        return com.uniminuto.clinica.util.PdfGenerator.crear("Atención médica y fórmula",lineas);
    }
}
