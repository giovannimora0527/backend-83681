package com.uniminuto.clinica.models;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponseDTO {
    private Long id;
    private String username;
    private String rol;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
    private String email;
    // OJO: no tiene passwordHash → nunca se expone
}