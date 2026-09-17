package com.uniminuto.clinica.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private LocalDateTime fechaCreacion;
    // no incluye passwordHash: nunca se expone por seguridad
}
