package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity @Table(name = "despacho") @Data
public class Despacho {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "numero", nullable = false, length = 40) private String numero;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 80) private String idempotencyKey;
    @Column(name = "request_hash", nullable = false, length = 64) private String requestHash;
    @Column(name = "estado", nullable = false, length = 32) private String estado;
    @Column(name = "usuario_id") private Long usuarioId;
    @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn;
    @Column(name = "confirmado_en") private LocalDateTime confirmadoEn;
}
