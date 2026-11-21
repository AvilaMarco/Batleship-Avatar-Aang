package dev.racnar.shipbender.integration;

import dev.racnar.shipbender.security.WebSecurityConfig;

import dev.racnar.shipbender.TestcontainersConfiguration;
import dev.racnar.shipbender.integration.support.AuthHelper;
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
    @ValueSource(strings = {"/", "/index.html", "/assets/index-abc.js", "/favicon.ico", "/swagger-ui/index.html"})
    void publicPaths_withoutToken_areNotForbidden ( String path ) throws Exception {
        int s = mvc.perform(request(HttpMethod.GET, path)).andReturn().getResponse().getStatus();
        // sin token, una ruta no permitida responde 401 (anonimo), no 403: se descartan ambos
        assertNotEquals(401, s, path);
        assertNotEquals(403, s, path);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET /api/games", "GET /api/games/1", "PATCH /api/players/me"})
    void protectedPaths_withoutToken_are401 ( String route ) throws Exception {
        String[] r = route.split(" ");
        mvc.perform(request(HttpMethod.valueOf(r[0]), r[1])).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/inventado", "/h2-console/", "/web/games.html",
        "/webjars/stomp-websocket/2.3.4/stomp.min.js", "/libraries/x.js"})
    void unlistedPaths_areForbidden_evenWithPlayerToken ( String path ) throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        mvc.perform(request(HttpMethod.GET, path).header("Authorization", token)).andExpect(status().isForbidden());
    }
}
