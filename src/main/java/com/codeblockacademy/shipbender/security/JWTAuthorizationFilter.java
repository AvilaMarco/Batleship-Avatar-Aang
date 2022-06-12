package com.codeblockacademy.shipbender.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.codeblockacademy.shipbender.utils.ENV_VARIABLES;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JWTAuthorizationFilter extends OncePerRequestFilter implements ENV_VARIABLES {

    /**
     * Filtro para solicitar validación por token
     *
     * @param request  petition of client
     * @param response response for client
     * @param chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal ( HttpServletRequest request, HttpServletResponse response, FilterChain chain ) throws ServletException, IOException {
        try {

            if (!this.existeJWTToken(request)) SecurityContextHolder.clearContext();
            else {
                DecodedJWT decodedJWT = this.decodedValidJWT(request);

                if (decodedJWT == null) SecurityContextHolder.clearContext();
                else this.setUpSpringAuthentication(decodedJWT);
            }
            chain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    /**
     * Método para validar el token
     *
     * @param request petition of client
     * @return if the token is valid, return the roles of user authenticated
     */
    private DecodedJWT decodedValidJWT ( HttpServletRequest request ) {
        String jwtToken = request.getHeader(HEADER)
          .replace(PREFIX, "");

        JWTVerifier verifier = JWT.require(ALGORITHM)
          .build();
        return verifier.verify(jwtToken);
    }

    /**
     * Función para autenticarnos dentro del flujo de Spring
     *
     * @param decodedJWT object with token's data (authorities and username)
     */
    private void setUpSpringAuthentication ( DecodedJWT decodedJWT ) {

        List<SimpleGrantedAuthority> authorities = getAuthorities(decodedJWT);

        String username = decodedJWT.getSubject();

        var auth = new UsernamePasswordAuthenticationToken(
          username,
          null,
          authorities
        );
        SecurityContextHolder
          .getContext()
          .setAuthentication(auth);
    }

    /**
     * Verifica si existe la cabecera "Authorization" y cuyo valor comience con "Bearer"
     *
     * @param request petition of client
     * @return boolean indicate if the request contain a token
     */
    private boolean existeJWTToken ( HttpServletRequest request ) {
        String authenticationHeader = request.getHeader(HEADER);
        return authenticationHeader != null && authenticationHeader.startsWith(PREFIX);
    }

    private List<SimpleGrantedAuthority> getAuthorities ( DecodedJWT decodedJWT ) {
        return decodedJWT
          .getClaim(CLAIMS)
          .asList(String.class)
          .stream()
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList());
    }
}