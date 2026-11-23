package com.uniminuto.clinica.entity;

<<<<<<< HEAD
import jakarta.persistence.*;
=======
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
import lombok.Data;

import java.time.LocalDateTime;

<<<<<<< HEAD
@Entity
@Table(name = "anotacion_historia")
@Data
=======
/**
 * Anotación de la evolución clínica de una historia. Cada anotación queda con fecha, veterinario y descripción.
 */
@Entity
@Data
@Table(name = "anotacion_historia")
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
public class AnotacionHistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @JoinColumn(name = "historia_id", nullable = false)
    @ManyToOne
    private HistoriaMedica historia;

    @JoinColumn(name = "medico_id", nullable = false)
    @ManyToOne
    private Medico medico;

    @JoinColumn(name = "cita_id", nullable = false)
    @ManyToOne
    private Cita cita;

    @Column(name = "fecha", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fecha;
=======
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "historia_id", nullable = false)
    private HistoriaMedica historia;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fechaHora;
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

<<<<<<< HEAD
    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
=======
    @Column(name = "observacion", nullable = false, columnDefinition = "TEXT")
    private String observacion;

    @Column(name = "diagnostico_evolucion", nullable = false, columnDefinition = "TEXT")
    private String diagnosticoEvolucion;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
