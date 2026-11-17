package com.uniminuto.clinica.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

/** Define permisos de acceso a las rutas del backend según el perfil autenticado. */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login", "/auth/validate-token", "/error").permitAll()
                        .requestMatchers("/auth/cerrar-sesion").authenticated()
                        .requestMatchers("/inventario/dispensaciones/**")
                        .access(perfiles("ADMIN", "ADMINISTRADOR", "DESPACHO_MEDICO"))
                        .requestMatchers("/inventario/carga-masiva/**").access(administradores())
                        .requestMatchers(HttpMethod.GET,
                                "/inventario/medicamentos/informes/**", "/medicamento/informes/**")
                        .access(perfiles("ADMIN", "ADMINISTRADOR", "DESPACHO_MEDICO"))
                        .requestMatchers("/inventario/medicamentos/**", "/medicamento/**")
                        .access(permisosMedicamento())
                        .requestMatchers("/inventario-lote/**").access(permisosLotes())
                        .requestMatchers("/movimiento-inventario/**").access(soloLectura("ADMIN",
                                "ADMINISTRADOR", "DESPACHO_MEDICO"))
                        .requestMatchers("/formula-medica/**").access(permisosFormula())
                        .requestMatchers("/historia-clinica/**", "/anotacion-historia/**")
                        .access(perfiles("ADMIN", "ADMINISTRADOR", "MEDICO", "VETERINARIO"))
                        .requestMatchers("/cita/**")
                        .access(perfiles("ADMIN", "ADMINISTRADOR", "RECEPCION", "RECEPCIONISTA",
                                "MEDICO", "VETERINARIO"))
                        .requestMatchers("/cliente/**", "/mascota/**").access(permisosClienteYMascota())
                        .requestMatchers("/medico/**").access(permisosLecturaRecepcion())
                        .requestMatchers("/raza/**").access(permisosRaza())
                        .requestMatchers("/factura/**", "/pago/**")
                        .access(perfiles("ADMIN", "ADMINISTRADOR", "RECEPCION", "RECEPCIONISTA"))
                        .requestMatchers(HttpMethod.GET, "/reporte/financiero/**", "/reportes/financieros/**")
                        .access(perfiles("ADMIN", "ADMINISTRADOR", "RECEPCION", "RECEPCIONISTA"))
                        .anyRequest().access(administradores()))
                .addFilterBefore(jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Permite operaciones de inventario de medicamentos según el perfil y el método HTTP. */
    private AuthorizationManager<RequestAuthorizationContext> permisosMedicamento() {
        return (authentication, context) -> {
            Authentication actual = authentication.get();
            String metodo = context.getRequest().getMethod();
            boolean permitido = esMetodoLectura(metodo)
                    ? tienePerfil(actual, "ADMIN", "ADMINISTRADOR", "MEDICO", "VETERINARIO", "DESPACHO_MEDICO")
                    : esMetodoEscritura(metodo) && tienePerfil(actual, "ADMIN", "ADMINISTRADOR");
            return new AuthorizationDecision(permitido);
        };
    }

    /** Permite consulta de lotes a administración y despacho, y escrituras a administración. */
    private AuthorizationManager<RequestAuthorizationContext> permisosLotes() {
        return (authentication, context) -> {
            Authentication actual = authentication.get();
            boolean permitido = esMetodoLectura(context.getRequest().getMethod())
                    ? tienePerfil(actual, "ADMIN", "ADMINISTRADOR", "DESPACHO_MEDICO")
                    : esMetodoEscritura(context.getRequest().getMethod())
                    && tienePerfil(actual, "ADMIN", "ADMINISTRADOR");
            return new AuthorizationDecision(permitido);
        };
    }

    /** Permite leer fórmulas a despacho y administra CRUD para médicos y administradores. */
    private AuthorizationManager<RequestAuthorizationContext> permisosFormula() {
        return (authentication, context) -> {
            Authentication actual = authentication.get();
            String metodo = context.getRequest().getMethod();
            boolean permitido = tienePerfil(actual, "ADMIN", "ADMINISTRADOR", "MEDICO", "VETERINARIO")
                    || esMetodoLectura(metodo) && tienePerfil(actual, "DESPACHO_MEDICO");
            return new AuthorizationDecision(permitido);
        };
    }

    /** Limita consulta de clientes y mascotas para médicos, manteniendo CRUD de recepción y administración. */
    private AuthorizationManager<RequestAuthorizationContext> permisosClienteYMascota() {
        return (authentication, context) -> {
            Authentication actual = authentication.get();
            String metodo = context.getRequest().getMethod();
            boolean permitido = tienePerfil(actual, "ADMIN", "ADMINISTRADOR", "RECEPCION", "RECEPCIONISTA")
                    || esMetodoLectura(metodo) && tienePerfil(actual, "MEDICO", "VETERINARIO");
            return new AuthorizationDecision(permitido);
        };
    }

    /** Limita los datos de médicos a consultas de recepción y administración. */
    private AuthorizationManager<RequestAuthorizationContext> permisosLecturaRecepcion() {
        return (authentication, context) -> {
            Authentication actual = authentication.get();
            boolean administrador = tienePerfil(actual, "ADMIN", "ADMINISTRADOR");
            boolean recepcionConsulta = esMetodoLectura(context.getRequest().getMethod())
                    && tienePerfil(actual, "RECEPCION", "RECEPCIONISTA");
            return new AuthorizationDecision(administrador || recepcionConsulta);
        };
    }

    /** Permite a recepción consultar razas para registrar mascotas y reserva su administración a Admin. */
    private AuthorizationManager<RequestAuthorizationContext> permisosRaza() {
        return (authentication, context) -> {
            Authentication actual = authentication.get();
            boolean administrador = tienePerfil(actual, "ADMIN", "ADMINISTRADOR");
            boolean consultaRecepcion = esMetodoLectura(context.getRequest().getMethod())
                    && tienePerfil(actual, "RECEPCION", "RECEPCIONISTA");
            return new AuthorizationDecision(administrador || consultaRecepcion);
        };
    }

    /** Devuelve un control de acceso que permite cualquiera de los perfiles indicados. */
    private AuthorizationManager<RequestAuthorizationContext> perfiles(String... perfilesPermitidos) {
        return (authentication, context) -> new AuthorizationDecision(
                tienePerfil(authentication.get(), perfilesPermitidos));
    }

    /** Devuelve un control que limita la ruta a lecturas de los perfiles indicados. */
    private AuthorizationManager<RequestAuthorizationContext> soloLectura(String... perfilesPermitidos) {
        return (authentication, context) -> new AuthorizationDecision(
                esMetodoLectura(context.getRequest().getMethod())
                        && tienePerfil(authentication.get(), perfilesPermitidos));
    }

    /** Devuelve un control de acceso exclusivo para perfiles administrativos. */
    private AuthorizationManager<RequestAuthorizationContext> administradores() {
        return perfiles("ADMIN", "ADMINISTRADOR");
    }

    /** Comprueba una autoridad normalizada del contexto JWT contra los perfiles permitidos. */
    private boolean tienePerfil(Authentication autenticacion, String... perfilesPermitidos) {
        if (!estaAutenticado(autenticacion)) {
            return false;
        }
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(this::normalizarPerfil)
                .anyMatch(autoridad -> Arrays.stream(perfilesPermitidos)
                        .map(this::normalizarPerfil)
                        .anyMatch(perfil -> autoridad.equals("ROLE_" + perfil)));
    }

    /** Comprueba que el contexto corresponda a una sesión autenticada real. */
    private boolean estaAutenticado(Authentication autenticacion) {
        return autenticacion != null && autenticacion.isAuthenticated()
                && !(autenticacion instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
    }

    /** Identifica verbos HTTP que no modifican recursos. */
    private boolean esMetodoLectura(String metodo) {
        return HttpMethod.GET.matches(metodo) || HttpMethod.HEAD.matches(metodo);
    }

    /** Identifica verbos HTTP que modifican recursos. */
    private boolean esMetodoEscritura(String metodo) {
        return HttpMethod.POST.matches(metodo) || HttpMethod.PUT.matches(metodo)
                || HttpMethod.PATCH.matches(metodo) || HttpMethod.DELETE.matches(metodo);
    }

    /** Normaliza perfiles y autoridades para soportar tildes, espacios y variantes de nombres. */
    private String normalizarPerfil(String nombre) {
        return Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
