package com.uniminuto.clinica.config;

import com.uniminuto.clinica.entity.Session;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.SessionRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final SessionRepository sessionRepository;
    private final String jwtSecret;

    public JwtAuthenticationFilter(UsuarioRepository usuarioRepository,
                                   SessionRepository sessionRepository,
                                   @Value("${JWT_SECRET}") String jwtSecret) {
        this.usuarioRepository = usuarioRepository;
        this.sessionRepository = sessionRepository;
        this.jwtSecret = jwtSecret;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/auth/login") || path.equals("/auth/validate-token")
                || request.getMethod().equalsIgnoreCase("OPTIONS");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.length() <= 7) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
            Jws<Claims> signedClaims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            if (!"HS512".equals(signedClaims.getHeader().getAlgorithm())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            long userId = Long.parseLong(signedClaims.getPayload().getSubject());
            String claimedRole = signedClaims.getPayload().get("rol", String.class);
            Usuario usuario = usuarioRepository.findOneById(userId).orElse(null);
            Session session = sessionRepository.findByToken(token).orElse(null);
            if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())
                    || usuario.getPerfil() == null || !Boolean.TRUE.equals(usuario.getPerfil().getActivo())
                    || session == null || !userIdEquals(session, userId)
                    || session.getFechaExpiracion() == null
                    || session.getFechaExpiracion().isBefore(LocalDateTime.now())
                    || claimedRole == null
                    || !claimedRole.equalsIgnoreCase(usuario.getPerfil().getNombre())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String role = usuario.getPerfil().getNombre().trim().toUpperCase(Locale.ROOT);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(Long.toString(userId), null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean userIdEquals(Session session, long userId) {
        return session.getUserId() != null && session.getUserId() == userId;
    }
}
