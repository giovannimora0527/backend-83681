package com.uniminuto.clinica.config;

import com.uniminuto.clinica.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"message\":\"Autenticación requerida.\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"FORBIDDEN\",\"message\":\"No tienes permiso para esta operación.\"}");
                        }))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/validate-token",
                                "/auth/recover-password", "/auth/reset-password").permitAll()
                        .requestMatchers("/auth/cerrar-sesion").authenticated()
                        .requestMatchers("/usuario/**", "/email/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/facturacion/tarifas/admin").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/facturacion/tarifas")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/facturacion/tarifas/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/facturacion/tarifas/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers("/facturacion/**")
                        .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        .requestMatchers("/reportes/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO", "RECEPCIONISTA")
                        .requestMatchers(HttpMethod.POST, "/cita/*/cerrar")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                        .requestMatchers(HttpMethod.GET, "/medicamento/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO", "DESPACHO_MEDICO")
                        .requestMatchers("/medicamento/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/formula-medica/listar")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                        .requestMatchers(HttpMethod.POST, "/formula-medica/*/dispensar")
                        .hasAnyRole("ADMINISTRADOR", "DESPACHO_MEDICO")
                        .requestMatchers("/formula-medica/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                        .requestMatchers("/inventario/despachos/**")
                        .hasAnyRole("ADMINISTRADOR", "DESPACHO_MEDICO")
                        .requestMatchers(HttpMethod.GET, "/inventario/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO", "DESPACHO_MEDICO")
                        .requestMatchers("/inventario/**").hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                        .requestMatchers("/historia-clinica/**", "/anotacion-historia/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                        .requestMatchers("/cita/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO", "RECEPCIONISTA")
                        .requestMatchers("/cliente/**", "/mascota/**", "/medico/**", "/raza/**", "/especializacion/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO", "RECEPCIONISTA")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ALLOWED_ORIGINS:http://localhost:4200}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService noDefaultUserDetailsService() {
        return username -> { throw new UsernameNotFoundException("La autenticación se realiza mediante JWT."); };
    }
}
