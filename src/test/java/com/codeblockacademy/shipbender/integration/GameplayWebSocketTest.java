package com.codeblockacademy.shipbender.integration;

import com.codeblockacademy.shipbender.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;
import com.codeblockacademy.shipbender.integration.support.AuthHelper;
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

/** Caracterizacion de /app/{id}/ships -> /topic/gameplay/{id}/ships con JWT en el CONNECT de STOMP. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GameplayWebSocketTest {

    static final ObjectMapper JSON = new JsonMapper();
    static final String FIVE_SHIPS = """
      [{"type":"CARRIER","locations":["A1","A2","A3","A4","A5"]},
       {"type":"BATTLESHIP","locations":["B1","B2","B3","B4"]},
       {"type":"SUBMARINE","locations":["C1","C2","C3"]},
       {"type":"DESTROYER","locations":["D1","D2","D3"]},
       {"type":"PATROL_BOAT","locations":["E1","E2"]}]""";

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
        String loc = "W" + UUID.randomUUID().toString().substring(0, 8);
        String body = mvc.perform(post("/api/match/games/FIRE/{l}", loc).header("Authorization", a))
          .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = JSON.readTree(body).get("game_id").asLong();
        mvc.perform(post("/api/match/games/{id}", id).header("Authorization", b)).andExpect(status().isOk());
        return id;
    }

    static Set<String> keys ( JsonNode n ) {
        Set<String> s = new HashSet<>();
        n.propertyNames().iterator().forEachRemaining(s::add);
        return s;
    }

    /** Estado del host segun REST (ShipDTO persistidos o no). */
    String hostStatus ( String token, long id ) throws Exception {
        String body = mvc.perform(get("/api/match/{id}/status", id).header("Authorization", token))
          .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JSON.readTree(body).at("/status/host").asString();
    }

    @Test
    void sendShips_validShips_arePersistedAndBroadcast_toHostAndRival () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        assertEquals("WITHOUT_SHIPS", hostStatus(a, id));
        BlockingQueue<String> evA = new LinkedBlockingQueue<>(), evB = new LinkedBlockingQueue<>();
        StompSession sa = connect(a, evA), sb = connect(b, evB);
        String topic = "/topic/gameplay/" + id + "/ships";
        BlockingQueue<String> qa = subscribe(sa, topic), qb = subscribe(sb, topic);

        sendJson(sa, "/app/" + id + "/ships", FIVE_SHIPS);

        assertNotNull(qa.poll(5, TimeUnit.SECONDS), "A recibe el mensaje");
        // bug conocido: se arregla en F4 (ADR 003) - el topic es compartido y el rival recibe el GameDataDTO de A
        assertNotNull(qb.poll(5, TimeUnit.SECONDS), "el rival tambien (fuga)");
        assertEquals("WITH_SHIPS", hostStatus(a, id));
    }

    @Test
    void sendShips_invalidShips_areAcceptedAndPersisted_knownBug () throws Exception {
        // bug conocido: ShipValidation no se usa; Z99, tamano incorrecto y superpuestos se guardan igual
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        StompSession sa = connect(a, new LinkedBlockingQueue<>());

        sendJson(sa, "/app/" + id + "/ships", """
          [{"type":"CARRIER","locations":["Z99"]},
           {"type":"PATROL_BOAT","locations":["A1","A2","A3","A4"]},
           {"type":"SUBMARINE","locations":["A1","A2","A3"]}]""");

        // sin broadcast que esperar: se sondea el estado REST con deadline
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        String host;
        while ("WITHOUT_SHIPS".equals(host = hostStatus(a, id)) && System.nanoTime() < deadline)
            java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(20));
        assertEquals("WITH_SHIPS", host);
    }

    @Test
    void connectWithoutToken_connectionIsClosedByServer () throws Exception {
        // WebSocketSecurityConfig deniega todo a no-PLAYER: el CONNECT sin JWT termina en conexion cerrada
        ExecutionException e = assertThrows(ExecutionException.class, () -> connect(null, new LinkedBlockingQueue<>()));
        assertInstanceOf(ConnectionLostException.class, e.getCause());
    }
}
