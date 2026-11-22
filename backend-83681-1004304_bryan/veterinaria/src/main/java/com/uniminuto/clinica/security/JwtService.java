package com.uniminuto.clinica.security;

import com.uniminuto.clinica.exception.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/** Valida la firma y la expiración del JWT emitido en el login (HS512). */
@Component
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public AuthUser parse(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Long id = Long.valueOf(c.getSubject());
            String rol = Roles.normalize(c.get("rol", String.class));
            return new AuthUser(id, c.get("username", String.class), c.get("email", String.class), rol);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadRequestException("La sesión es inválida o expiró. Inicie sesión nuevamente.",
                    HttpStatus.UNAUTHORIZED);
        }
    }
}
