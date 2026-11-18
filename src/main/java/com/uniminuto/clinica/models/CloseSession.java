package com.uniminuto.clinica.models;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(example = """
        {
          "usuarioId": 4
        }
        """)
@Data
public class CloseSession {
    private Long usuarioId;
}
