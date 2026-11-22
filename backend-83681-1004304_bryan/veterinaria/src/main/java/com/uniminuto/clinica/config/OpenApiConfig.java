package com.uniminuto.clinica.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger/OpenAPI con autenticación Bearer JWT. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clinicaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Clínica Veterinaria – Proyecto integrador")
                        .version("1.0")
                        .description("Inventario, calendario del veterinario, atención médica, fórmulas, facturación, "
                                + "reportes y despacho. Autentíquese en /auth (usuario + OTP) y pulse «Authorize» con el JWT."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
