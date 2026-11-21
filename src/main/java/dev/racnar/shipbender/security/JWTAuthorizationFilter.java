package dev.racnar.shipbender.security;


import com.auth0.jwt.interfaces.DecodedJWT;
import dev.racnar.shipbender.config.ENV_VARIABLES;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class JWTAuthorizationFilter extends OncePerRequestFilter implements ENV_VARIABLES {

    private final JWTUtil jwtUtil;

    public JWTAuthorizationFilter ( JWTUtil jwtUtil ) {
        this.jwtUtil = jwtUtil;
    }

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
        if (!this.existeJWTToken(request)) SecurityContextHolder.clearContext();
        else {
            DecodedJWT decodedJWT = jwtUtil.decodedValidJWT(request.getHeader(HEADER));

            if (decodedJWT == null) SecurityContextHolder.clearContext();
            else this.setUpSpringAuthentication(decodedJWT);
        }
        chain.doFilter(request, response);
    }
    /**
     * Función para autenticarnos dentro del flujo de Spring
     *
     * @param decodedJWT object with token's data (authorities and username)
     */
    private void setUpSpringAuthentication ( DecodedJWT decodedJWT ) {

        List<SimpleGrantedAuthority> authorities = jwtUtil.getAuthorities(decodedJWT);

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
}