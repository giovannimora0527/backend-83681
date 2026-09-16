package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String userName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "rol")
    private String rol;

    @Column (name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column (name = "activo")
    private Boolean activo;

    @Column(name = "email")
    private String email;
}