package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class Anotacion_HistoriaRS {

    /** Código numérico de resultado (e.j. 200 para éxito). */
    private int status;

    /** Mensaje legible con información del resultado. */
    private String message;

}
