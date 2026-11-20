package com.uniminuto.clinica.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity @Table(name = "inventario_importacion") @Data
public class InventarioImportacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "archivo", nullable = false, length = 255) private String archivo;
    @Column(name = "usuario_id") private Long usuarioId;
    @Column(name = "total_filas", nullable = false) private Integer totalFilas;
    @Column(name = "validas", nullable = false) private Integer validas;
    @Column(name = "invalidas", nullable = false) private Integer invalidas;
    @Column(name = "estado", nullable = false, length = 20) private String estado;
    @Column(name = "resultado_json", columnDefinition = "json") private String resultadoJson;
    @Column(name = "creado_en", nullable = false) private LocalDateTime creadoEn;
    @Column(name = "confirmado_en") private LocalDateTime confirmadoEn;
}
