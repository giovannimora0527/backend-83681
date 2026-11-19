package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * Clase que representa la solicitud para crear o actualizar una mascota.
 */
@Schema(example = """
        {
          "nombre": "Nube",
          "edad": 4,
          "razaId": 3,
          "clienteId": 11
        }
        """)
@Data
public class MascotaRq {

    /**
     * Id unico de la mascota.
     */
    private Long mascotaId;

    /**
     * Nombre de la mascota.
     */
    private String nombre;

    /**
     * Edad de la mascota.
     */
    private Integer edad;

    /**
     * Identificador de la raza de la mascota.
     */
    private Integer razaId;

    /**
     * Identificador del cliente propietario de la mascota.
     */
    private Long clienteId;

}
