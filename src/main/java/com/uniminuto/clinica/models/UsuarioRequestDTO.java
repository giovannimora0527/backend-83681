package com.uniminuto.clinica.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequestDTO {

    private Long id; // requerido solo al actualizar

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    private String password; // texto plano, se cifra en el service (MD5)
}
