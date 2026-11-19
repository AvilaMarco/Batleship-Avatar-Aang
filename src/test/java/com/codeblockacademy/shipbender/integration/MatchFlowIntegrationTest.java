package com.codeblockacademy.shipbender.integration;

import com.codeblockacademy.shipbender.integration.support.AuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Caracterizacion del contrato REST actual de games y match. Cada test crea sus propios datos. */
@SpringBootTest
@AutoConfigureMockMvc
class MatchFlowIntegrationTest {

    @Autowired MockMvc mvc;

    static String uniqueLocation () { return "T" + UUID.randomUUID().toString().substring(0, 8); }

    /** Crea una partida con el token dado y devuelve su game_id. */
    long createGame ( String token, String location ) throws Exception {
        String body = mvc.perform(post("/api/match/games/FIRE/{loc}", location).header("Authorization", token))
          .andExpect(status().isCreated())
          .andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(body).get("game_id").asLong();
    }

    @Test
    void listGames_withoutToken_isForbidden () throws Exception {
        mvc.perform(get("/api/games")).andExpect(status().isForbidden());
    }

    @Test
    void listGames_withPlayerToken_returnsPlayerAndGames () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        mvc.perform(get("/api/games").header("Authorization", a))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.player").exists())
          .andExpect(jsonPath("$.games").isArray());
    }

    @Test
    void createGame_returnsCreatedWithGameAndGamePlayerIds () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        mvc.perform(post("/api/match/games/FIRE/{loc}", uniqueLocation()).header("Authorization", a))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.game_id").isNumber())
          .andExpect(jsonPath("$.game_player_id").isNumber());
    }

    @Test
    void createGame_sameLocationTwice_isConflict () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        String loc = uniqueLocation();
        createGame(a, loc);
        mvc.perform(post("/api/match/games/FIRE/{loc}", loc).header("Authorization", b))
          .andExpect(status().isConflict());
    }

    @Test
    void joinGame_secondPlayerJoins_thirdGetsConflictBecauseFull () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc), c = AuthHelper.registerAndLogin(mvc);
        long id = createGame(a, uniqueLocation());
        mvc.perform(post("/api/match/games/{id}", id).header("Authorization", b))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.game_id").value(id))
          .andExpect(jsonPath("$.game_player_id").isNumber());
        mvc.perform(post("/api/match/games/{id}", id).header("Authorization", c))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.error").value("Game Is Full"));
    }

    @Test
    void joinGame_ownGame_isAccepted_knownBug () throws Exception {
        // bug conocido: la validacion esta comentada en MatchService.joinGame (MatchService.java:58-59)
        String a = AuthHelper.registerAndLogin(mvc);
        long id = createGame(a, uniqueLocation());
        mvc.perform(post("/api/match/games/{id}", id).header("Authorization", a))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.game_id").value(id));
    }

    @Test
    void getMatchAndStatus_asParticipant_returnOkWithKeys () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        long id = createGame(a, uniqueLocation());
        mvc.perform(get("/api/match/{id}", id).header("Authorization", a))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id))
          .andExpect(jsonPath("$.nation").value("FIRE"))
          .andExpect(jsonPath("$.location").exists())
          .andExpect(jsonPath("$.turn").value(1))
          .andExpect(jsonPath("$.game_players").isArray());
        mvc.perform(get("/api/match/{id}/status", id).header("Authorization", a))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.id").value(id))
          .andExpect(jsonPath("$.status.game").value("CREATED"))
          .andExpect(jsonPath("$.status.host").value("WITHOUT_SHIPS"))
          .andExpect(jsonPath("$.status.client").value("WAITING"));
    }

    @Test
    void getMatchAndStatus_asNonParticipant_areConflict () throws Exception {
        // el mensaje observado es "Player Already in Game" aunque el jugador NO esta (nombre enganoso)
        String a = AuthHelper.registerAndLogin(mvc), c = AuthHelper.registerAndLogin(mvc);
        long id = createGame(a, uniqueLocation());
        mvc.perform(get("/api/match/{id}", id).header("Authorization", c)).andExpect(status().isConflict());
        mvc.perform(get("/api/match/{id}/status", id).header("Authorization", c)).andExpect(status().isConflict());
    }

    @Test
    void status_unknownGameId_isNotFound () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        mvc.perform(get("/api/match/{id}/status", 999999).header("Authorization", a))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.error").value("Game Not Found Exception"));
    }
}
