package com.uniminuto.clinica.models;
import lombok.Data;
@Data
public class CerrarConsultaRq {
    private Long medicoId;
    private String descripcion;
    private String diagnostico;
    private String observaciones;
}
