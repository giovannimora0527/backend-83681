package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity @Table(name = "cita_historial") @Data
public class CitaHistorial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "cita_id", nullable = false) private Long citaId;
    @Column(name = "estado_anterior", length = 20) private String estadoAnterior;
    @Column(name = "estado_nuevo", nullable = false, length = 20) private String estadoNuevo;
    @Column(name = "nota", length = 500) private String nota;
    @Column(name = "usuario_id") private Long usuarioId;
    @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn;
}
