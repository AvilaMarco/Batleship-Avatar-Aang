package dev.racnar.shipbender.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter ( JwtService jwtService ) {
        this.jwtService = jwtService;
    }

    /** Autentica si el header trae un JWT valido; si no, sigue sin autenticar y la cadena responde 401. */
    @Override
    protected void doFilterInternal ( HttpServletRequest request, HttpServletResponse response, FilterChain chain ) throws ServletException, IOException {
        String header = request.getHeader(JwtService.HEADER);
        SecurityContextHolder.clearContext();
        if (jwtService.existeJWTToken(header)) {
            try {
                DecodedJWT jwt = jwtService.decodedValidJWT(header);
                SecurityContextHolder.getContext().setAuthentication(
                  new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, jwtService.getAuthorities(jwt)));
            } catch (JWTVerificationException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
