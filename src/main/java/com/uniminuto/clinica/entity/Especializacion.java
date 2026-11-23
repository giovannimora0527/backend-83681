package com.uniminuto.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

<<<<<<< HEAD
=======
/**
 * Entidad especialización médica (catálogo).
 */
@Entity
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
@Data
@Entity
@Table(name = "especializacion")
public class Especializacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

<<<<<<< HEAD
    @Column(name = "nombre")
=======
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

<<<<<<< HEAD
    @Column(name = "codigo_especializacion")
=======
    @Column(name = "codigo_especializacion", nullable = false, unique = true, length = 10)
>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    private String codigoEspecializacion;
}
