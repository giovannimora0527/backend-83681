package com.uniminuto.clinica.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para retornar los datos de la anotación médica ordenados.
 */
@Data
@JsonPropertyOrder({
        "id",
        "historiaId",
        "medicoId",
        "fecha",
        "descripcion"
})
public class AnotacionHistoriaRs {
    private Long id;
    private Long historiaId;
    private Integer medicoId;
    private LocalDateTime fecha;
    private String descripcion;
}
