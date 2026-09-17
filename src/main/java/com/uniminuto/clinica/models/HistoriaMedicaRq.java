// models/HistoriaMedicaRq.java
package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class HistoriaMedicaRq {
    private Long id;
    private Long mascotaId;
    private String observacionesGenerales;
}