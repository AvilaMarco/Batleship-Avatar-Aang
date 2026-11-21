package dev.racnar.shipbender.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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
public class StompAuthInterceptor implements ChannelInterceptor {

    @Autowired
    JwtService jwtService;

    @Override
    public Message<?> preSend ( final Message<?> message, final MessageChannel channel ) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT == accessor.getCommand()) {

            String token = accessor.getFirstNativeHeader(JwtService.HEADER);
            if (jwtService.existeJWTToken(token)) {
                try {
                    accessor.setUser(getUserByToken(jwtService.decodedValidJWT(token)));
                } catch (JWTVerificationException ignored) {
                    // sin usuario: la regla de autorización rechaza el CONNECT
                }
            }
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken getUserByToken ( DecodedJWT decodedJWT ) {
        String                       user        = decodedJWT.getSubject();
        List<SimpleGrantedAuthority> authorities = jwtService.getAuthorities(decodedJWT);
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
}
