// models/CitaRq.java
package com.uniminuto.clinica.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CitaRq {
    private Long id;
    private Long mascotaId;
    private Long medicoId;
    private LocalDateTime fecha;
    private String motivo;
    private String observaciones;
}