package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class ClinicaFacturaRs {
    private String nombre;
    private String nit;
    private String direccion;
    private String telefono;
    private String correo;
    private Boolean datosDemostracion;
}
