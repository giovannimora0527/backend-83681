package com.uniminuto.clinica.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name = "despacho_historial") @Data
public class DespachoHistorial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "despacho_id", nullable = false) private Long despachoId;
    @Column(name = "estado_anterior", length = 32) private String estadoAnterior;
    @Column(name = "estado_nuevo", nullable = false, length = 32) private String estadoNuevo;
    @Column(name = "motivo", length = 500) private String motivo;
    @Column(name = "usuario_id") private Long usuarioId;
    @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn;
}
