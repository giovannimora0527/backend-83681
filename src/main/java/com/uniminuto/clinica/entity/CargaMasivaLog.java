package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

/** Registra la prevalidación y confirmación de una carga masiva de inventario. */
@Entity
@Table(name = "carga_masiva_log")
@Data
public class CargaMasivaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tipo_archivo", nullable = false, length = 10)
    private String tipoArchivo;

    @Column(name = "total_registros", nullable = false)
    private Integer totalRegistros;

    @Column(name = "registros_validos", nullable = false)
    private Integer registrosValidos;

    @Column(name = "registros_erroneos", nullable = false)
    private Integer registrosErroneos;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    @Column(name = "errores_json", columnDefinition = "JSON")
    private String erroresJson;

    @Column(name = "archivo_errores_url", length = 500)
    private String archivoErroresUrl;
}
