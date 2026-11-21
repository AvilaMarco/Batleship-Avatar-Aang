package dev.racnar.shipbender.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtService {

    static final String HEADER = "Authorization";
    static final String PREFIX = "Bearer ";
    private static final String CLAIMS = "AUTHORITIES";

    private final Algorithm algorithm;
    private final long expirationMinutes;

    public JwtService ( @Value("${jwt.secret}") String secret,
                        @Value("${jwt.expiration-minutes}") long expirationMinutes ) {
        if (secret == null || secret.length() < 32)
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres");
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationMinutes = expirationMinutes;
    }

    /** Genera un token (con prefijo "Bearer ") para el usuario y sus roles. */
    public String getJWTToken ( String username, List<String> roles ) {

        LocalDateTime expired = LocalDateTime.now()
          .plusMinutes(expirationMinutes);
        Date expiredTime = Date.from(expired.atZone(ZoneId.systemDefault())
          .toInstant());

        String token = JWT.create()
          .withSubject(username)
          .withExpiresAt(expiredTime)
          .withClaim(CLAIMS, roles)
          .sign(algorithm);

        return PREFIX + token;
    }

    /** Verifica firma y vencimiento; lanza JWTVerificationException si el token no es valido. */
    public DecodedJWT decodedValidJWT ( String token ) {
        JWTVerifier verifier = JWT.require(algorithm)
          .build();
        return verifier.verify(token.replace(PREFIX, ""));
    }

    public List<SimpleGrantedAuthority> getAuthorities ( DecodedJWT decodedJWT ) {
        return decodedJWT
          .getClaim(CLAIMS)
          .asList(String.class)
          .stream()
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
    }

    public boolean existeJWTToken ( String token ) {
        return token != null && token.startsWith(PREFIX);
    }
}
