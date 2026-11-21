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

/** Contrato STOMP v2 (ADR 004 Anexo B): conexion, eventos de /topic/games/{id}, emotes y errores. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GameEventsWebSocketTest {

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
          .flatMap(se -> se.getSubscriptions().stream()).filter(x -> dest.equals(x.getDestination())
            // el broker reescribe /user/queue/x a /queue/x-user<session>
            || dest.startsWith("/user/") && x.getDestination().startsWith(dest.substring(5))).count();
    }

    long createAndJoin ( String a, String b ) throws Exception {
        long id = AuthHelper.createGame(mvc, a);
        AuthHelper.join(mvc, b, id);
        return id;
    }

    static final String FLEET = """
      {"ships":[{"type":"CARRIER","cells":["A1","A2","A3","A4","A5"]},
                {"type":"BATTLESHIP","cells":["B1","B2","B3","B4"]},
                {"type":"SUBMARINE","cells":["C1","C2","C3"]},
                {"type":"DESTROYER","cells":["D1","D2","D3"]},
                {"type":"PATROL_BOAT","cells":["E1","E2"]}]}""";

    /** Espera el siguiente mensaje JSON; falla (rojo "el evento no llego") si no hay ninguno en 3 s. */
    JsonNode next ( BlockingQueue<String> q ) throws Exception {
        String m = q.poll(3, TimeUnit.SECONDS);
        assertNotNull(m, "no llego ningun mensaje en 3 s");
        return JSON.readTree(m);
    }

    void placeShips ( String token, long id, String fleet, int expected ) throws Exception {
        mvc.perform(post("/api/games/{id}/ships", id).header("Authorization", token)
          .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(fleet))
          .andExpect(status().is(expected));
    }

    long playerId ( String token ) throws Exception {
        String body = mvc.perform(get("/api/players/me").header("Authorization", token))
          .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JSON.readTree(body).get("id").asLong();
    }

    @Test
    void playerJoins_hostReceivesPlayerJoinedWithoutEmail () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        BlockingQueue<String> events = subscribe(connect(a, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        AuthHelper.join(mvc, b, id);

        JsonNode e = next(events);
        assertEquals("PLAYER_JOINED", e.path("type").asString());
        assertEquals(playerId(b), e.path("player").path("id").asLong());
        assertTrue(e.path("player").path("email").isMissingNode(), "PlayerSummary no lleva email");
    }

    @Test
    void shipsPlaced_rivalReceivesShipsPlacedWithoutAnyCell () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        BlockingQueue<String> rival = subscribe(connect(b, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        placeShips(a, id, FLEET, 201);

        String raw = rival.poll(3, TimeUnit.SECONDS);
        assertNotNull(raw, "no llego SHIPS_PLACED");
        JsonNode e = JSON.readTree(raw);
        assertEquals("SHIPS_PLACED", e.path("type").asString());
        assertEquals(playerId(a), e.path("player_id").asLong());
        for (String leak : List.of("A1", "cells", "ships")) assertFalse(raw.contains(leak), "el evento filtra " + leak);
    }

    @Test
    void bothPlaceShips_bothReceiveShipsPlacedThenGameStartedWithTurn1 () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        BlockingQueue<String> qa = subscribe(connect(a, new LinkedBlockingQueue<>()), "/topic/games/" + id);
        BlockingQueue<String> qb = subscribe(connect(b, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        placeShips(a, id, FLEET, 201);
        placeShips(b, id, FLEET, 201);

        for (BlockingQueue<String> q : List.of(qa, qb)) {
            assertEquals("SHIPS_PLACED", next(q).path("type").asString());
            assertEquals("SHIPS_PLACED", next(q).path("type").asString());
            JsonNode started = next(q);
            assertEquals("GAME_STARTED", started.path("type").asString());
            assertEquals(1, started.path("turn").asInt());
        }
    }

    @Test
    void invalidFleet_is400AndNoEventIsPublished () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        BlockingQueue<String> events = subscribe(connect(b, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        placeShips(a, id, "{\"ships\":[{\"type\":\"CARRIER\",\"cells\":[\"Z99\"]}]}", 400);

        assertNull(events.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void emote_participantSends_bothReceiveEmoteWithSenderId () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        StompSession sa = connect(a, new LinkedBlockingQueue<>());
        BlockingQueue<String> qa = subscribe(sa, "/topic/games/" + id);
        BlockingQueue<String> qb = subscribe(connect(b, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        sendJson(sa, "/app/games/" + id + "/emotes", "{\"emote\":\"emote-1\"}");

        long idA = playerId(a);
        for (BlockingQueue<String> q : List.of(qa, qb)) {
            JsonNode e = next(q);
            assertEquals("EMOTE", e.path("type").asString());
            assertEquals(idA, e.path("player_id").asLong());
            assertEquals("emote-1", e.path("emote").asString());
        }
    }

    @Test
    void emote_fromNonParticipant_errorQueueGetsNotAParticipant_andHostGetsNothing () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc), c = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        BlockingQueue<String> qa = subscribe(connect(a, new LinkedBlockingQueue<>()), "/topic/games/" + id);
        StompSession sc = connect(c, new LinkedBlockingQueue<>());
        BlockingQueue<String> errors = subscribe(sc, "/user/queue/errors");
        String dest = "/app/games/" + id + "/emotes";

        sendJson(sc, dest, "{\"emote\":\"emote-1\"}");

        JsonNode p = next(errors);
        assertEquals("NOT_A_PARTICIPANT", p.path("code").asString());
        assertEquals(dest, p.path("instance").asString());
        assertNull(qa.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void emote_blank_errorQueueGetsValidationFailed () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        StompSession sa = connect(a, new LinkedBlockingQueue<>());
        BlockingQueue<String> errors = subscribe(sa, "/user/queue/errors");

        sendJson(sa, "/app/games/" + id + "/emotes", "{\"emote\":\"\"}");

        assertEquals("VALIDATION_FAILED", next(errors).path("code").asString());
    }

    // ---- salvos (ADR 004 Anexo F, v2.1; rojos hasta la tarea 413) ----

    static final String[] SINK_ALL = {
      "[\"A1\",\"A2\",\"A3\",\"A4\",\"A5\"]", "[\"B1\",\"B2\",\"B3\",\"B4\",\"C1\"]",
      "[\"C2\",\"C3\",\"D1\",\"D2\",\"D3\"]", "[\"E1\",\"E2\",\"F1\",\"F2\",\"F3\"]"};
    static final String[] ALL_MISSES = {
      "[\"G1\",\"G2\",\"G3\",\"G4\",\"G5\"]", "[\"G6\",\"G7\",\"G8\",\"G9\",\"G10\"]",
      "[\"H1\",\"H2\",\"H3\",\"H4\",\"H5\"]", "[\"H6\",\"H7\",\"H8\",\"H9\",\"H10\"]"};

    void fire ( String token, long id, String cellsJson ) throws Exception {
        mvc.perform(post("/api/games/{id}/salvos", id).header("Authorization", token)
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"cells\":" + cellsJson + "}"))
          .andExpect(status().isCreated());
    }

    /** Partida IN_GAME; el GAME_STARTED ya paso antes de que el test se suscriba. */
    long startedGame ( String a, String b ) throws Exception {
        long id = createAndJoin(a, b);
        placeShips(a, id, FLEET, 201);
        placeShips(b, id, FLEET, 201);
        return id;
    }

    /** Salta eventos hasta el tipo pedido (max 20); rojo "el evento no llego" si no aparece. */
    JsonNode nextOfType ( BlockingQueue<String> q, String type ) throws Exception {
        for (int i = 0; i < 20; i++) {
            JsonNode e = next(q);
            if (type.equals(e.path("type").asString())) return e;
        }
        return fail("no llego el evento " + type);
    }

    @Test
    void firstSalvo_rivalReceivesSalvoFiredWithoutCells () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = startedGame(a, b);
        BlockingQueue<String> rival = subscribe(connect(b, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        fire(a, id, ALL_MISSES[0]);

        String raw = rival.poll(3, TimeUnit.SECONDS);
        assertNotNull(raw, "no llego SALVO_FIRED");
        JsonNode e = JSON.readTree(raw);
        assertEquals("SALVO_FIRED", e.path("type").asString());
        assertEquals(playerId(a), e.path("player_id").asLong());
        assertEquals(1, e.path("turn").asInt());
        assertFalse(raw.contains("G1") || raw.contains("cells"), "SALVO_FIRED no lleva celdas");
    }

    @Test
    void secondSalvo_bothReceiveTurnResolvedWithHitsMissesAndSunk () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = startedGame(a, b);
        BlockingQueue<String> qa = subscribe(connect(a, new LinkedBlockingQueue<>()), "/topic/games/" + id);
        BlockingQueue<String> qb = subscribe(connect(b, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        fire(a, id, SINK_ALL[0]);
        fire(b, id, ALL_MISSES[0]);

        long idA = playerId(a);
        for (BlockingQueue<String> q : List.of(qa, qb)) {
            JsonNode e = nextOfType(q, "TURN_RESOLVED");
            assertEquals(1, e.path("turn").asInt());
            assertEquals(2, e.path("results").size());
            JsonNode mine = e.path("results").get(0).path("player_id").asLong() == idA
              ? e.path("results").get(0) : e.path("results").get(1);
            assertEquals(5, mine.path("hits").size());
            assertEquals(0, mine.path("misses").size());
            assertEquals("CARRIER", mine.path("sunk").get(0).asString());
        }
    }

    @Test
    void sinkingWholeFleet_bothReceiveGameFinishedWithScores_andProfileReflectsPoints () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = startedGame(a, b);
        BlockingQueue<String> qa = subscribe(connect(a, new LinkedBlockingQueue<>()), "/topic/games/" + id);
        BlockingQueue<String> qb = subscribe(connect(b, new LinkedBlockingQueue<>()), "/topic/games/" + id);

        for (int t = 0; t < 4; t++) {
            fire(a, id, SINK_ALL[t]);
            fire(b, id, ALL_MISSES[t]);
        }

        long idA = playerId(a), idB = playerId(b);
        for (BlockingQueue<String> q : List.of(qa, qb)) {
            JsonNode e = nextOfType(q, "GAME_FINISHED");
            assertEquals(idA, e.path("winner_id").asLong());
            assertEquals(3, e.path("scores").path(String.valueOf(idA)).asInt());
            assertEquals(0, e.path("scores").path(String.valueOf(idB)).asInt());
        }
        mvc.perform(get("/api/players/me").header("Authorization", a))
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.stats.score").value(3));
        mvc.perform(get("/api/players/me").header("Authorization", b))
          .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.stats.score").value(0));
    }

    @Test
    void subscribeToV1ShipsTopic_isDenied () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = createAndJoin(a, b);
        BlockingQueue<String> events = new LinkedBlockingQueue<>();
        StompSession s = connect(a, events);

        s.subscribe("/topic/gameplay/" + id + "/ships", new StompSessionHandlerAdapter() {});

        // denegado = frame ERROR o conexion cerrada (llega a events); la regla /topic/** de hoy lo permite
        assertNotNull(events.poll(2, TimeUnit.SECONDS), "la suscripcion v1 no fue denegada");
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
