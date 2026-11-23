package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entidad cliente (dueño de una o más mascotas).
 */
@Entity
@Data
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cliente_id")
<<<<<<< HEAD
    private Long id;

    @Column(name = "usuario_id", unique = true)
    private Integer usuarioId;

=======
    private Long clienteId;

>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 1)
    private String genero;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
