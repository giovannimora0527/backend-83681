package com.uniminuto.clinica.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DisponibilidadRs {
    private Long medicoId;
    private LocalDate fecha;
    private Integer duracionMinutos;
    private List<LocalDateTime> horariosDisponibles;
    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public List<LocalDateTime> getHorariosDisponibles() { return horariosDisponibles; }
    public void setHorariosDisponibles(List<LocalDateTime> horariosDisponibles) { this.horariosDisponibles = horariosDisponibles; }
}
