package com.uniminuto.clinica.models;

import lombok.Data;
<<<<<<< HEAD
import java.time.LocalDate;

@Data
public class ClienteRq {
    private Long id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String genero;
    private String telefono;
    private String direccion;
=======

import java.time.LocalDate;

/**
 * Datos de entrada para registrar o actualizar un cliente.
 */
@Data
public class ClienteRq {

    private Long clienteId;

    private String tipoDocumento;

    private String numeroDocumento;

    private String nombres;

    private String apellidos;

    private LocalDate fechaNacimiento;

    private String genero;

    private String telefono;

    private String direccion;

>>>>>>> 69d248a2c289f0849669bc921e5317721d7eb661
    private Boolean activo;
}
