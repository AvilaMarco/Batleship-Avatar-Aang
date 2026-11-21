package dev.racnar.shipbender.integration;

import dev.racnar.shipbender.TestcontainersConfiguration;
import dev.racnar.shipbender.integration.support.AuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Contrato REST v2 de partidas y barcos (ADR 004 A.2-A.4, Anexos C y E). Cada test crea sus propios datos. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class GameRestIntegrationTest {

    static final String VALID_FLEET = """
      {"ships":[{"type":"CARRIER","cells":["A1","A2","A3","A4","A5"]},
                {"type":"BATTLESHIP","cells":["B1","B2","B3","B4"]},
                {"type":"SUBMARINE","cells":["C1","C2","C3"]},
                {"type":"DESTROYER","cells":["D1","D2","D3"]},
                {"type":"PATROL_BOAT","cells":["E1","E2"]}]}""";

    /** Z99 fuera de grilla, tamano incorrecto y solapes. */
    static final String INVALID_FLEET = """
      {"ships":[{"type":"CARRIER","cells":["Z99"]},
                {"type":"PATROL_BOAT","cells":["A1","A2","A3","A4"]},
                {"type":"SUBMARINE","cells":["A1","A2","A3"]}]}""";

    @Autowired MockMvc mvc;

    static String uniqueLocation () { return "T" + UUID.randomUUID().toString().substring(0, 8); }

    ResultActions placeShips ( String token, long id, String fleet ) throws Exception {
        return mvc.perform(post("/api/games/{id}/ships", id).header("Authorization", token)
          .contentType(MediaType.APPLICATION_JSON).content(fleet));
    }

    ResultActions getGame ( String token, long id ) throws Exception {
        return mvc.perform(get("/api/games/{id}", id).header("Authorization", token));
    }

    ResultActions join ( String token, long id ) throws Exception {
        return mvc.perform(post("/api/games/{id}/players", id).header("Authorization", token));
    }

    @Test
    void listGames_withoutToken_isUnauthorized () throws Exception {
        mvc.perform(get("/api/games")).andExpect(status().isUnauthorized());
    }

    @Test
    void listGames_withPlayerToken_returnsSummariesWithoutEmail () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        AuthHelper.createGame(mvc, a);
        mvc.perform(get("/api/games").header("Authorization", a))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[*].players[*].email").doesNotExist());
    }

    @Test
    void createGame_returnsCreatedWithLocationAndOwnView () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        mvc.perform(post("/api/games").header("Authorization", a).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nation\":\"FIRE\",\"location\":\"" + uniqueLocation() + "\"}"))
          .andExpect(status().isCreated())
          .andExpect(header().exists("Location"))
          .andExpect(jsonPath("$.status").value("CREATED"))
          .andExpect(jsonPath("$.opponent").value(nullValue()))
          .andExpect(jsonPath("$.me.ships_placed").value(false));
    }

    @Test
    void createGame_sameLocationTwice_is409LocationTaken () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        String body = "{\"nation\":\"FIRE\",\"location\":\"" + uniqueLocation() + "\"}";
        mvc.perform(post("/api/games").header("Authorization", a).contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isCreated());
        mvc.perform(post("/api/games").header("Authorization", b).contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("LOCATION_TAKEN"));
    }

    @Test
    void joinGame_secondPlayerJoins_thirdGets409GameFull () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc), c = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        join(b, id).andExpect(status().isCreated())
          .andExpect(jsonPath("$.opponent.player.id").exists());
        join(c, id).andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("GAME_FULL"));
    }

    @Test
    void joinGame_ownGame_is409AlreadyInGame () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        join(a, id).andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("ALREADY_IN_GAME"));
    }

    @Test
    void joinGame_unknownId_is404GameNotFound () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        join(a, 999999).andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("GAME_NOT_FOUND"));
    }

    @Test
    void getGame_asParticipant_returnsViewRelativeToCaller () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        getGame(a, id).andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id))
          .andExpect(jsonPath("$.nation").value("FIRE"))
          .andExpect(jsonPath("$.status").value("CREATED"))
          .andExpect(jsonPath("$.turn").value(1))
          .andExpect(jsonPath("$.me.player.id").exists());
    }

    @Test
    void getGame_asNonParticipant_is403NotAParticipant () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), c = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        getGame(c, id).andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("NOT_A_PARTICIPANT"));
    }

    @Test
    void getGame_unknownId_is404GameNotFound () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        getGame(a, 999999).andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("GAME_NOT_FOUND"));
    }

    // ---- barcos ----

    @Test
    void placeShips_validFleet_returnsCreatedWithFiveOwnShips () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        placeShips(a, id, VALID_FLEET).andExpect(status().isCreated())
          .andExpect(jsonPath("$.me.ships_placed").value(true))
          .andExpect(jsonPath("$.me.ships", hasSize(5)));
    }

    @Test
    void placeShips_secondTime_is409ShipsAlreadyPlaced () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        placeShips(a, id, VALID_FLEET).andExpect(status().isCreated());
        placeShips(a, id, VALID_FLEET).andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("SHIPS_ALREADY_PLACED"));
    }

    @Test
    void placeShips_invalidFleet_is400InvalidFleet_andNothingIsSaved () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        placeShips(a, id, INVALID_FLEET).andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("INVALID_FLEET"));
        getGame(a, id).andExpect(jsonPath("$.me.ships_placed").value(false));
    }

    @Test
    void placeShips_asNonParticipant_is403 () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), c = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        placeShips(c, id, VALID_FLEET).andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("NOT_A_PARTICIPANT"));
    }

    @Test
    void placeShips_bothPlayers_startsGame_ownShipsSurvive_rivalShipsNeverExposed () throws Exception {
        String a = AuthHelper.registerAndLogin(mvc), b = AuthHelper.registerAndLogin(mvc);
        long id = AuthHelper.createGame(mvc, a);
        AuthHelper.join(mvc, b, id);
        placeShips(a, id, VALID_FLEET).andExpect(status().isCreated());
        placeShips(b, id, VALID_FLEET).andExpect(status().isCreated());
        // sospecha GameService.java:39-46 (ADR 005 Anexo C): al iniciar la partida A podria perder sus barcos
        getGame(a, id).andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("IN_GAME"))
          .andExpect(jsonPath("$.me.ships", hasSize(5)))
          .andExpect(jsonPath("$.opponent.ships_placed").value(true))
          .andExpect(jsonPath("$.opponent.ships").doesNotExist());
    }
}
