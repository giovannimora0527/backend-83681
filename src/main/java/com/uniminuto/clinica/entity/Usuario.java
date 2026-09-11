package com.uniminuto.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String rol;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    private Boolean activo;

    @Column(nullable = false)
    private String email;
}