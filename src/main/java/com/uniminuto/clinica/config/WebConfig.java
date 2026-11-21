package com.uniminuto.clinica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SeguridadInterceptor seguridadInterceptor;

    public WebConfig(SeguridadInterceptor seguridadInterceptor) {
        this.seguridadInterceptor = seguridadInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:4200",
                        "http://localhost:4201"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seguridadInterceptor)
                .addPathPatterns("/**")
                // Rutas publicas: login, pruebas, Swagger y errores
                .excludePathPatterns(
                        "/auth/**",
                        "/clinica/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/error");
    }
}