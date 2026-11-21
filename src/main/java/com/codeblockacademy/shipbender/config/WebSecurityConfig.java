package com.codeblockacademy.shipbender.config;


import com.codeblockacademy.shipbender.security.JWTAuthorizationFilter;
import com.codeblockacademy.shipbender.security.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
            .requestMatchers("/users/admin").hasAnyAuthority("ADMIN")
            .requestMatchers("/api/players/nation/**", "/api/match/**", "/topic/**", "/app/**", "/web/games")
            .hasAnyAuthority("PLAYER")
            .requestMatchers("/api/games/**").hasAnyAuthority("GUEST", "PLAYER")
            .requestMatchers("/api/players/login", "/docs", "/configuration/ui", "/swagger-resources/**",
              "/configuration/security", "/swagger-ui.html", "/webjars/**")
            .permitAll()
            .anyRequest().permitAll())
          // if logout is successful, just send a success response
          .logout(l -> l.logoutUrl("/api/players/logout")
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));
        return http.build();
    }

}