package com.uniminuto.clinica.config;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.function.Supplier;

@Configuration
public class SecurityConfig {

    private static final SimpleGrantedAuthority DESPACHO_ROLE =
            new SimpleGrantedAuthority("ROLE_DESPACHO_MEDICO");

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
                        .requestMatchers("/inventario/dispensaciones/**").hasRole("DESPACHO_MEDICO")
                        .requestMatchers("/inventario/**").access(inventarioAuthorization())
                        .anyRequest().access(nonDispatchAuthorization()))
                .addFilterBefore(jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private AuthorizationManager<RequestAuthorizationContext> inventarioAuthorization() {
        return (authentication, context) -> {
            Authentication current = authentication.get();
            if (!isAuthenticated(current)) {
                return new AuthorizationDecision(false);
            }
            if (!current.getAuthorities().contains(DESPACHO_ROLE)) {
                return new AuthorizationDecision(true);
            }
            HttpServletRequest request = context.getRequest();
            String path = request.getServletPath();
            boolean readOnlyInventory = request.getMethod().equals("GET")
                    && (path.matches("/inventario/medicamentos/\\d+/(disponibilidad|lotes|movimientos)")
                    || path.equals("/inventario/medicamentos/disponibilidad"));
            return new AuthorizationDecision(readOnlyInventory);
        };
    }

    private AuthorizationManager<RequestAuthorizationContext> nonDispatchAuthorization() {
            return (authentication, context) -> {
            Authentication current = authentication.get();
            return new AuthorizationDecision(isAuthenticated(current)
                    && !current.getAuthorities().contains(DESPACHO_ROLE));
        };
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
    }
}
