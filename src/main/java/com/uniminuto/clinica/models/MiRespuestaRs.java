package com.uniminuto.clinica.models;

import lombok.Data;

@Data
public class MiRespuestaRs {

    private int status;
    private String message;
    private Object data;

}