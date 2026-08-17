package gg.users.userapps.infrastructure.adapters.out.services;

import gg.users.userapps.domain.model.User;
import gg.users.userapps.domain.ports.out.JwtServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtServiceAdapter implements JwtServicePort {

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long accessExpTime;

    @Value("${jwt.expiration.refresh}")
    private long refreshExpTime;

    @Override
    public String generateAccessToken(User user, List<String> roles) {
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getMiliseconds(accessExpTime)))
                .signWith(this.getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + getMiliseconds(refreshExpTime)))
                .signWith(this.getSigningKey())
                .compact();
    }

    @Override
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static long getMiliseconds(long timeInMinutes) {
        return timeInMinutes * 60 * 1000;
    }
}
