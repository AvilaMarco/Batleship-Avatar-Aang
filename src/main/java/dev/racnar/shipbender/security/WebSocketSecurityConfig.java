package dev.racnar.shipbender.security;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Manual config (not @EnableWebSocketSecurity): it forces CSRF on CONNECT; auth here is a JWT STOMP header.
@Configuration
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor stompAuthInterceptor;
    private final ApplicationContext context;

    public WebSocketSecurityConfig ( StompAuthInterceptor stompAuthInterceptor, ApplicationContext context ) {
        this.stompAuthInterceptor = stompAuthInterceptor;
        this.context = context;
    }

    @Override
    public void configureClientInboundChannel ( ChannelRegistration registration ) {
        AuthorizationManager<Message<?>> rules = MessageMatcherDelegatingAuthorizationManager.builder()
          .nullDestMatcher().authenticated()
          .simpDestMatchers("/app/games/*/emotes").hasAuthority("PLAYER")
          .simpSubscribeDestMatchers("/topic/games/*", "/user/queue/errors").hasAuthority("PLAYER")
          .anyMessage().denyAll()
          .build();

        AuthorizationChannelInterceptor authz = new AuthorizationChannelInterceptor(rules);
        authz.setAuthorizationEventPublisher(new SpringAuthorizationEventPublisher(context));

        registration.interceptors(stompAuthInterceptor, new SecurityContextChannelInterceptor(), authz);
    }
}
