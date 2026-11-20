package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity @Table(name = "despacho_reversion") @Data
public class DespachoReversion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "despacho_id", nullable = false, unique = true) private Long despachoId;
    @Column(name = "motivo", nullable = false, length = 500) private String motivo;
    @Column(name = "autorizado_por", nullable = false) private Long autorizadoPor;
    @Column(name = "creada_en", nullable = false) private LocalDateTime creadaEn;
}
