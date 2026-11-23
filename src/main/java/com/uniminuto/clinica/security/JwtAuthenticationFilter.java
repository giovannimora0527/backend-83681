package com.uniminuto.clinica.security;

import com.uniminuto.clinica.repository.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SessionRepository sessionRepository;
    private final SecretKey signingKey;

    public JwtAuthenticationFilter(
            SessionRepository sessionRepository,
            @Value("${JWT_SECRET}") String jwtSecret) {
        this.sessionRepository = sessionRepository;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String username = claims.get("username", String.class);
            String role = claims.get("rol", String.class);
            var session = sessionRepository.findByTokenHashAndFechaExpiracionAfter(
                    TokenHashUtil.sha256Hex(token), LocalDateTime.now());
            if (subject == null || username == null || role == null || session.isEmpty()
                    || !subject.equals(String.valueOf(session.get().getUserId()))) {
                writeUnauthorized(response);
                return;
            }

            String authority = "ROLE_" + role.trim().toUpperCase(Locale.ROOT);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(new SimpleGrantedAuthority(authority)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response);
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"message\":\"La sesión no es válida o ha expirado.\"}");
    }
}
