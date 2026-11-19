package com.uniminuto.clinica.models;

import lombok.Data;

/** Contiene el resultado del cierre de sesión. */
@Data
public class CloseSessionRs {
    private String message;
    private boolean success;
}
