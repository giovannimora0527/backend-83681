package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa los tokens OTP para validación de login.
 * Mapea la tabla otp_token de la base de datos clinica.
 * @author gmora
 */
@Entity
@Table(name = "otp_token")
@Data
public class OtpToken {

    /**
     * Identificador único del token OTP.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    /**
     * ID del usuario al que pertenece el token.
     */
    @Column(name = "user_id", nullable = false)
    private Long usuarioId;

    /**
     * Código OTP de 6 caracteres alfanuméricos.
     */
    @Column(name = "code", nullable = false, length = 6)
    private String codigo;

    /**
     * Fecha y hora de generación del token.
     */
    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha y hora de expiración del token.
     */
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    /**
     * Número de intentos fallidos de validación.
     */
    @Column(name = "intentos", nullable = false)
    private Integer intentos;

    /**
     * Indica si el token ya fue utilizado.
     */
    @Column(name = "usado", nullable = false)
    private Boolean usado;
}