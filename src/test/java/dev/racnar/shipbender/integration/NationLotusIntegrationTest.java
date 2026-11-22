package dev.racnar.shipbender.integration;

import dev.racnar.shipbender.TestcontainersConfiguration;
import dev.racnar.shipbender.integration.support.AuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Nacion LOTUS (V4): aceptada en jugador y partida; invalidas siguen 400; la migracion solo ensancha el check. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class NationLotusIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @Test
    void patchMe_withLotus_returnsProfileWithLotus () throws Exception {
        String t = AuthHelper.registerAndLogin(mvc);
        mvc.perform(patch("/api/players/me").header("Authorization", t)
            .contentType(MediaType.APPLICATION_JSON).content("{\"nation\":\"LOTUS\"}"))
          .andExpect(status().isOk()).andExpect(jsonPath("$.nation").value("LOTUS"));
    }

    @Test
    void createGame_withLotus_isCreated () throws Exception {
        String t = AuthHelper.registerAndLogin(mvc);
        mvc.perform(post("/api/games").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nation\":\"LOTUS\",\"location\":\"T" + UUID.randomUUID().toString().substring(0, 8) + "\"}"))
          .andExpect(status().isCreated()).andExpect(jsonPath("$.nation").value("LOTUS"));
    }

    @Test
    void createGame_withUnknownNation_is400 () throws Exception {
        String t = AuthHelper.registerAndLogin(mvc);
        mvc.perform(post("/api/games").header("Authorization", t).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nation\":\"PLUTO\",\"location\":\"X1\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void migrationV4_keepsPreviousNationsAndAllowsLotus () {
        for (String table : new String[]{"game", "player"}) {
            String def = jdbc.queryForObject(
              "select pg_get_constraintdef(oid) from pg_constraint where conname = ?", String.class, table + "_nation_check");
            assertThat(def).contains("WATER", "AIR", "FIRE", "EARTH", "LOTUS");
        }
    }
}
