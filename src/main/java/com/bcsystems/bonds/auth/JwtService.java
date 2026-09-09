package com.bcsystems.bonds.auth;

import com.bcsystems.bonds.domain.Persona;
import com.bcsystems.bonds.domain.Permiso;
import com.bcsystems.bonds.domain.Token;
import com.bcsystems.bonds.domain.en.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Token generateToken(Persona persona) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", persona.getNombre() + " " + persona.getApellido());
        claims.put("rol", persona.getRol().getNombre());
        claims.put("permisos", new ArrayList<>(getAllPermissions(persona)));

        String accessToken = buildToken(claims, persona.getUsuario(), jwtExpiration);
        String refreshToken = buildToken(new HashMap<>(), persona.getUsuario(), refreshExpiration);

        return Token.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type(TokenType.BEARER)
                .isRevoked(false)
                .isExpired(false)
                .persona(persona)
                .build();
    }

    private Set<String> getAllPermissions(Persona persona) {
        Set<String> permisos = persona.getRol().getPermisos().stream()
                .map(Permiso::getClave)
                .collect(Collectors.toSet());

        if (persona.getPermisosAdicionales() != null) {
            persona.getPermisosAdicionales().stream()
                    .map(pa -> pa.getPermiso().getClave())
                    .forEach(permisos::add);
        }

        return permisos;
    }

    public Set<String> extractPermissions(String token) {
        Claims claims = extractAllClaims(token);
        List<String> permisos = claims.get("permisos", List.class);
        return permisos != null ? new LinkedHashSet<>(permisos) : new LinkedHashSet<>();
    }

    private String buildToken(Map<String, Object> extraClaims, String username, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, Persona persona) {
        final String username = extractUsername(token);
        return (username.equals(persona.getUsuario())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}