package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_cargue_masivo")
@Data
public class LogCargueMasivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "fecha_cargue", nullable = false)
    private LocalDateTime fechaCargue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_responsable_id", nullable = false)
    private Usuario usuarioResponsable;

    @Column(name = "total_registros_procesados", nullable = false)
    private Integer totalRegistrosProcesados;

    @Column(name = "registros_exitosos", nullable = false)
    private Integer registrosExitosos;

    @Column(name = "registros_con_error", nullable = false)
    private Integer registrosConError;

    @Column(name = "detalle_errores", columnDefinition = "TEXT")
    private String detalleErrores;
}
