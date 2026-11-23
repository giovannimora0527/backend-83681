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
@Table(name = "historia_medica")
@Data
=======
/**
 * Entidad historia clínica. Pertenece a una mascota de un cliente y se origina en una cita atendida.
 */
@Entity
@Data
@Table(name = "historia_medica")
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
public class HistoriaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< HEAD
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Mascota mascota;

    @Column(name = "fecha_creacion", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;
=======
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "cita_id", nullable = false, unique = true)
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "diagnostico", nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
