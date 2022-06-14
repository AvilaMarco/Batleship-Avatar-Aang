package com.codeblockacademy.shipbender.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.codeblockacademy.shipbender.config.ENV_VARIABLES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthChannelInterceptorAdapter implements ChannelInterceptor, ENV_VARIABLES {

    @Autowired
    JWTUtil jwtUtil;

    @Override
    public Message<?> preSend ( final Message<?> message, final MessageChannel channel ) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT == accessor.getCommand()) {

            String token = accessor.getFirstNativeHeader(HEADER);
            if (jwtUtil.existeJWTToken(token)) {
                DecodedJWT                          decodedJWT = jwtUtil.decodedValidJWT(token);
                UsernamePasswordAuthenticationToken userAuth   = this.getUserByToken(decodedJWT);

                accessor.setUser(userAuth);
            }
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken getUserByToken ( DecodedJWT decodedJWT ) {
        String                       user        = decodedJWT.getSubject();
        List<SimpleGrantedAuthority> authorities = jwtUtil.getAuthorities(decodedJWT);
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
}