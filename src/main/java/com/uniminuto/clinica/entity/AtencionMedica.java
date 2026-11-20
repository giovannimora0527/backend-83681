package com.uniminuto.clinica.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name = "atencion_medica", uniqueConstraints = @UniqueConstraint(name = "uk_atencion_cita", columnNames = "cita_id")) @Data
public class AtencionMedica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "cita_id", nullable = false) private Long citaId;
    @Column(name = "mascota_id", nullable = false) private Integer mascotaId;
    @Column(name = "medico_id", nullable = false) private Long medicoId;
    @Column(name = "motivo", columnDefinition = "TEXT") private String motivo;
    @Column(name = "sintomas", columnDefinition = "TEXT") private String sintomas;
    @Column(name = "diagnostico", columnDefinition = "TEXT") private String diagnostico;
    @Column(name = "observaciones", columnDefinition = "TEXT") private String observaciones;
    @Column(name = "recomendaciones", columnDefinition = "TEXT") private String recomendaciones;
    @Column(name = "atendida_en", nullable = false) private LocalDateTime atendidaEn;
}
