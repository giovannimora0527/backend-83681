package com.uniminuto.clinica.entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity @Table(name = "medico_usuario") @Data
public class MedicoUsuario {
    @Id @Column(name = "medico_id") private Long medicoId;
    @Column(name = "usuario_id", nullable = false, unique = true) private Long usuarioId;
}
