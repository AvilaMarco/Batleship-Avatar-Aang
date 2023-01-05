package com.codeblockacademy.shipbender.dto.config;


import com.codeblockacademy.shipbender.security.JWTAuthorizationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure ( HttpSecurity http ) throws Exception {
        http.csrf()
          .disable()
          .addFilterAfter(new JWTAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
          .authorizeRequests()

          .antMatchers(
            "/users/admin"
          )
          .hasAnyAuthority("ADMIN")

          .antMatchers(
            "/api/players/nation/**",
            "/api/match/**",
            "/topic/**",
            "/app/**"
          )
          .hasAnyAuthority("PLAYER")

          .antMatchers(
            "/api/games/**"
          )
          .hasAnyAuthority("GUEST", "PLAYER")

          .antMatchers(
            "/api/players/login",
            "/docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/h2-console/**"
          )
          .permitAll()
          .anyRequest()
          .permitAll();

        http.logout()
          .logoutUrl("/api/players/logout");

        // if login is successful, just clear the flags asking for authentication
        http.formLogin()
          .successHandler(( req, res, auth ) -> {
              clearAuthenticationAttributes(req);
              res.sendRedirect("/web/games.html");
          });


        // if logout is successful, just send a success response
        http.logout()
          .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes ( HttpServletRequest request ) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

}