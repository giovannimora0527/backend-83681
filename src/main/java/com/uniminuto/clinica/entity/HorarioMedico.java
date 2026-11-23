package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "horario_medico")
@Data
public class HorarioMedico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;
    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
    @Column(nullable = false)
    private Boolean activo = true;
}
