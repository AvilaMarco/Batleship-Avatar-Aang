package com.codeoftheweb.salvo.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure ( HttpSecurity http ) throws Exception {
        /*http.authorizeRequests()*/
        //indico las direcciones a las quepuedeo acceder
        //.antMatchers("/rest/**").hasAuthority("ADMIN")
/*          .antMatchers("/api/gp/**")
          .hasAuthority("USER");*/

        http.formLogin()
          .usernameParameter("email")
          .passwordParameter("password")
          .loginPage("/api/login")
          .failureHandler(( request, response, exception ) -> {
              /*response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "no");*/
              throw new AuthenticationException("ñalksjd");
          });

        http.logout()
          .logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf()
          .disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling()
          .authenticationEntryPoint(( req, res, exc ) -> {
              /*res.sendError(HttpServletResponse.SC_UNAUTHORIZED);*/
              /*res.sendRedirect("/laksd");*/
              throw new AuthenticationException("CHAN");
          });

        // if login is successful, just clear the flags asking for authentication
        http.formLogin()
          .successHandler(( req, res, auth ) -> {
              clearAuthenticationAttributes(req);
              res.sendRedirect("/web/games.html");
          });

        // if login fails, just send an authentication failure response
        /*http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));*/

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