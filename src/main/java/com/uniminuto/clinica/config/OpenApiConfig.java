package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la descripción de la API para poder consultar las rutas y usar el token de sesión en
 * Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI apiClinica() {
        return new OpenAPI()
                .info(new Info().title("Clínica veterinaria Uniminuto").version("1"))
                .components(new Components().addSecuritySchemes("jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("jwt"));
    }
}
