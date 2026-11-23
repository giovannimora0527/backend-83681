package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad cita médica. Relaciona un cliente, una de sus mascotas y un médico.
 */
@Entity
@Table(name = "cita")
<<<<<<< HEAD
@Data
public class Cita {

=======
public class Cita {

    /** Estados mínimos que debe manejar una cita. */
    public static final String PENDIENTE = "Pendiente";
    public static final String CONFIRMADA = "Confirmada";
    public static final String PROGRAMADA = "Programada";
    public static final String ATENDIDA = "Atendida";
    public static final String CANCELADA = "Cancelada";
    public static final List<String> ESTADOS = List.of(PENDIENTE, CONFIRMADA, PROGRAMADA, ATENDIDA, CANCELADA);

>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;
<<<<<<< HEAD

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos = 30;
=======
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
}
