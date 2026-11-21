package dev.racnar.shipbender.integration;

import dev.racnar.shipbender.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;
import dev.racnar.shipbender.integration.support.AuthHelper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Conexion STOMP con JWT en el CONNECT (los eventos v2 se prueban en 408). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GameplayWebSocketTest {

    static final ObjectMapper JSON = new JsonMapper();
    @LocalServerPort int port;
    @Autowired MockMvc mvc;
    @Autowired org.springframework.messaging.simp.user.SimpUserRegistry userRegistry;
    final List<WebSocketStompClient> clients = new ArrayList<>();

    @AfterEach
    void stopClients () { clients.forEach(WebSocketStompClient::stop); }

    /** Conecta por WebSocket nativo; token null = sin header Authorization. */
    StompSession connect ( String token, BlockingQueue<String> events ) throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        // acepta cualquier content-type (el cliente envia application/json como String)
        client.setMessageConverter(new StringMessageConverter() {
            @Override protected boolean supportsMimeType ( org.springframework.messaging.MessageHeaders h ) { return true; }
        });
        clients.add(client);
        StompHeaders headers = new StompHeaders();
        if (token != null) headers.add("Authorization", token);
        return client.connectAsync("ws://localhost:" + port + "/the-last-shipbender", new WebSocketHttpHeaders(), headers,
          new StompSessionHandlerAdapter() {
              @Override public void handleException ( StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable e ) { events.add("EXCEPTION " + e); }
              @Override public void handleTransportError ( StompSession s, Throwable e ) { events.add("TRANSPORT_ERROR " + e); }
              @Override public void handleFrame ( StompHeaders h, Object p ) { events.add("ERROR_FRAME " + h.getFirst("message")); }
          }).get(5, TimeUnit.SECONDS);
    }

    /** Suscribe y espera (polling con deadline, sin sleep) a que el broker registre la suscripcion. */
    BlockingQueue<String> subscribe ( StompSession s, String dest ) {
        BlockingQueue<String> q = new LinkedBlockingQueue<>();
        long before = countSubs(dest);
        s.subscribe(dest, new StompFrameHandler() {
            @Override public Type getPayloadType ( StompHeaders h ) { return String.class; }
            @Override public void handleFrame ( StompHeaders h, Object p ) { q.add((String) p); }
        });
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (countSubs(dest) <= before) {
            assertTrue(System.nanoTime() < deadline, "el broker no registro la suscripcion a " + dest);
            java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
        return q;
    }

    /** El servidor convierte el payload a List<ShipDTO> solo con content-type JSON. */
    void sendJson ( StompSession s, String dest, String json ) {
        StompHeaders h = new StompHeaders();
        h.setDestination(dest);
        h.setContentType(org.springframework.util.MimeTypeUtils.APPLICATION_JSON);
        s.send(h, json);
    }

    long countSubs ( String dest ) {
        return userRegistry.getUsers().stream().flatMap(u -> u.getSessions().stream())
          .flatMap(se -> se.getSubscriptions().stream()).filter(x -> dest.equals(x.getDestination())).count();
    }

    long createAndJoin ( String a, String b ) throws Exception {
        long id = AuthHelper.createGame(mvc, a);
        AuthHelper.join(mvc, b, id);
        return id;
    }

    @Test
    void connectWithoutToken_connectionIsClosedByServer () throws Exception {
        // WebSocketSecurityConfig deniega todo a no-PLAYER: el CONNECT sin JWT termina en conexion cerrada
        ExecutionException e = assertThrows(ExecutionException.class, () -> connect(null, new LinkedBlockingQueue<>()));
        assertInstanceOf(ConnectionLostException.class, e.getCause());
    }

    @Test
    void connectWithClientHeartbeat_serverRepliesWithConfiguredHeartbeat () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler scheduler = new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler();
        scheduler.initialize();
        client.setTaskScheduler(scheduler);
        client.setDefaultHeartbeat(new long[]{10000, 10000});
        clients.add(client);
        StompHeaders headers = new StompHeaders();
        headers.add("Authorization", a);
        java.util.concurrent.CompletableFuture<StompHeaders> connected = new java.util.concurrent.CompletableFuture<>();
        client.connectAsync("ws://localhost:" + port + "/the-last-shipbender", new WebSocketHttpHeaders(), headers,
          new StompSessionHandlerAdapter() {
              @Override public void afterConnected ( StompSession s, StompHeaders h ) { connected.complete(h); }
          }).get(5, TimeUnit.SECONDS);

        assertArrayEquals(new long[]{10000, 10000}, connected.get(5, TimeUnit.SECONDS).getHeartbeat());
        scheduler.shutdown();
    }
}
