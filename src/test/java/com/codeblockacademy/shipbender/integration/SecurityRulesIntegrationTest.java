package com.codeblockacademy.shipbender.integration;

import com.codeblockacademy.shipbender.TestcontainersConfiguration;
import com.codeblockacademy.shipbender.integration.support.AuthHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Caracterizacion de WebSecurityConfig: deny-by-default con rutas publicas explicitas. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesIntegrationTest {

    @Autowired MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/web/games.html", "/webjars/stomp-websocket/2.3.4/stomp.min.js", "/swagger-ui/index.html"})
    void publicPaths_withoutToken_areNotForbidden ( String path ) throws Exception {
        int s = mvc.perform(request(HttpMethod.GET, path)).andReturn().getResponse().getStatus();
        assertNotEquals(403, s, path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET /api/games", "GET /api/match/1", "POST /api/players/nation/FIRE"})
    void protectedPaths_withoutToken_areForbidden ( String route ) throws Exception {
        String[] r = route.split(" ");
        mvc.perform(request(HttpMethod.valueOf(r[0]), r[1])).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/inventado", "/h2-console/"})
    void unlistedPaths_areForbidden_evenWithPlayerToken ( String path ) throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        mvc.perform(request(HttpMethod.GET, path).header("Authorization", token)).andExpect(status().isForbidden());
    }
}
