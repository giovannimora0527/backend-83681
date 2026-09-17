// models/AnotacionHistoriaRq.java
package com.uniminuto.clinica.models;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AnotacionHistoriaRq {
    private Long id;
    private Long historiaMedicaId;
    private String descripcion;
    private LocalDateTime fecha;
}