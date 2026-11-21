package com.codeblockacademy.shipbender.config.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private TaskScheduler messageBrokerTaskScheduler;

    // @Lazy avoids the circular dependency with the scheduler Spring creates for the broker.
    @Autowired
    void setMessageBrokerTaskScheduler ( @Lazy TaskScheduler messageBrokerTaskScheduler ) {
        this.messageBrokerTaskScheduler = messageBrokerTaskScheduler;
    }

    @Override
    public void configureMessageBroker ( MessageBrokerRegistry config ) {
        config.enableSimpleBroker("/topic")
          .setHeartbeatValue(new long[]{10000, 10000})
          .setTaskScheduler(messageBrokerTaskScheduler);
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints ( StompEndpointRegistry registry ) {
        registry.addEndpoint("/the-last-shipbender");
    }

}
