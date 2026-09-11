package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Usuario;

import java.time.LocalDateTime;

public class UsuarioResponse {

    private Long id;
    private String username;
    private String rol;
    private LocalDateTime fechaCreacion;
    private Boolean activo;
    private String email;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.rol = usuario.getRol();
        this.fechaCreacion = usuario.getFechaCreacion();
        this.activo = usuario.getActivo();
        this.email = usuario.getEmail();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRol() {
        return rol;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public String getEmail() {
        return email;
    }
}
