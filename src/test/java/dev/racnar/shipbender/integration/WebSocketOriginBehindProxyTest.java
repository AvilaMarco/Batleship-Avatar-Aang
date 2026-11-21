package dev.racnar.shipbender.integration;

import dev.racnar.shipbender.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Tomcat (RemoteIpValve) fija el puerto 443 cuando X-Forwarded-Proto=https, por eso el Origin va sin puerto como el de cloudflared.
 * Detras de cloudflared: Host del test, Origin https y X-Forwarded-Proto: https (ADR 007 punto 6). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketOriginBehindProxyTest {

    @LocalServerPort int port;

    WebSocketSession handshake ( String origin ) throws Exception {
        WebSocketHttpHeaders h = new WebSocketHttpHeaders();
        h.setOrigin(origin);
        h.add("X-Forwarded-Proto", "https");
        return new StandardWebSocketClient()
          .execute(new TextWebSocketHandler(), h, java.net.URI.create("ws://localhost:" + port + "/the-last-shipbender"))
          .get(5, TimeUnit.SECONDS);
    }

    @Test
    void sameOriginOverHttps_connects () throws Exception {
        try (WebSocketSession s = handshake("https://localhost")) {
            assertTrue(s.isOpen());
        }
    }

    @Test
    void foreignOrigin_isRejected () {
        assertThrows(Exception.class, () -> handshake("https://evil.example"));
    }
}
