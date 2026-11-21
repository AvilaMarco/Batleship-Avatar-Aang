package dev.racnar.shipbender.config;


import dev.racnar.shipbender.security.JWTAuthorizationFilter;
import dev.racnar.shipbender.security.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    private final JWTUtil jwtUtil;

    WebSecurityConfig ( JWTUtil jwtUtil ) {
        this.jwtUtil = jwtUtil;
    }

    @Bean SecurityFilterChain filterChain ( HttpSecurity http ) throws Exception {
        http.csrf(csrf -> csrf.disable())
          .addFilterAfter(new JWTAuthorizationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
          .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/players/login", "/api/players").permitAll()
            .requestMatchers("/api/players/nation/**", "/api/match/**").hasAuthority("PLAYER")
            .requestMatchers("/api/games/**").hasAnyAuthority("GUEST", "PLAYER")
            // front viejo: el JWT está en localStorage, al navegar no se manda header
            .requestMatchers("/", "/index.html", "/favicon.ico", "/main.ogg", "/code/**", "/img/**",
              "/libraries/**", "/web/**", "/webjars/**").permitAll()
            // la autenticación del WebSocket se hace en el CONNECT de STOMP, no en el handshake
            .requestMatchers("/the-last-shipbender/**").permitAll()
            .requestMatchers("/swagger", "/swagger-ui/**", "/docs/**").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().denyAll())
          // if logout is successful, just send a success response
          .logout(l -> l.logoutUrl("/api/players/logout")
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));
        return http.build();
    }

}