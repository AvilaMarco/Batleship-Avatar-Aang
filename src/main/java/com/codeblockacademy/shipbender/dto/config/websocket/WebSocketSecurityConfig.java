package com.codeblockacademy.shipbender.dto.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSecurity
public class WebSocketSecurityConfig
  extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound ( MessageSecurityMetadataSourceRegistry messages ) {
        messages
          .nullDestMatcher()
          .authenticated()

          .simpDestMatchers("/app/**")
          .hasAnyAuthority("PLAYER")

          .simpSubscribeDestMatchers("/topic/**")
          .hasAnyAuthority("PLAYER")

          .anyMessage()
          .denyAll();
    }

    @Override
    protected boolean sameOriginDisabled () {
        return true;
    }
}