package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mascota")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mascota_id")
    private Long mascotaId;

    @Column (name = "nombre_mascota")
    private String nombreMascota;

    @Column (name = "edad")
    private Integer edad;

    @Column (name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column (name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne
    @JoinColumn(name = "raza_id")
    private Raza raza;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}
