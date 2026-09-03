package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
}
