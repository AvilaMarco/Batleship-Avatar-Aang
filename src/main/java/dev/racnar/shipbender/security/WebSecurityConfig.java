package dev.racnar.shipbender.security;

import dev.racnar.shipbender.shared.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final JwtService jwtService;
    private final JsonMapper jsonMapper;

    WebSecurityConfig ( JwtService jwtService, JsonMapper jsonMapper ) {
        this.jwtService = jwtService;
        this.jsonMapper = jsonMapper;
    }

    @Bean SecurityFilterChain filterChain ( HttpSecurity http ) throws Exception {
        http.csrf(csrf -> csrf.disable())
          .addFilterAfter(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
          .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/players").permitAll()
            // rutas listadas, no "/api/**": una ruta inexistente debe dar 403 (deny-by-default), no 404
            .requestMatchers("/api/players/**", "/api/games/**", "/api/match/**").hasAuthority("PLAYER")
            // front viejo: el JWT está en localStorage, al navegar no se manda header
            .requestMatchers("/", "/index.html", "/favicon.ico", "/main.ogg", "/code/**", "/img/**",
              "/libraries/**", "/web/**", "/webjars/**").permitAll()
            // la autenticación del WebSocket se hace en el CONNECT de STOMP, no en el handshake
            .requestMatchers("/the-last-shipbender/**").permitAll()
            .requestMatchers("/swagger", "/swagger-ui/**", "/docs/**").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().denyAll())
          .exceptionHandling(e -> e
            .authenticationEntryPoint((req, res, ex) -> write(req.getRequestURI(), res, ErrorCode.UNAUTHENTICATED, "Se requiere un token válido"))
            .accessDeniedHandler((req, res, ex) -> write(req.getRequestURI(), res, ErrorCode.FORBIDDEN, "Acceso denegado")));
        return http.build();
    }

    private void write ( String uri, HttpServletResponse res, ErrorCode code, String detail ) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(code.status()), detail);
        pd.setProperty("code", code.name());
        pd.setInstance(java.net.URI.create(uri));
        res.setStatus(code.status());
        res.setCharacterEncoding("UTF-8");
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        res.getWriter().write(jsonMapper.writeValueAsString(pd));
    }

}
