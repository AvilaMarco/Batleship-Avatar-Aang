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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashSet;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Contrato REST v2 de auth y jugadores (ADR 004 A.1, Anexo C). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    MockMvc mvc;

    private ResultActions postJson ( String url, String json ) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String creds ( String email, String password ) {
        return "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"Tester\"}".formatted(email, password);
    }

    private ResultActions patchNation ( String token, String nation ) throws Exception {
        return mvc.perform(patch("/api/players/me").header("Authorization", token)
          .contentType(MediaType.APPLICATION_JSON).content("{\"nation\":\"" + nation + "\"}"));
    }

    @Test
    void login_withSeedUser_returnsEmailAndBearerToken () throws Exception {
        postJson("/api/auth/login", creds("marco@admin.com", "123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email").value("marco@admin.com"))
          .andExpect(jsonPath("$.token", startsWith("Bearer ")));
    }

    @Test
    void login_withWrongPassword_returns401InvalidCredentials () throws Exception {
        postJson("/api/auth/login", creds("marco@admin.com", "wrong"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_withMalformedEmail_returns400ValidationFailedWithEmailError () throws Exception {
        postJson("/api/auth/login", creds("not-an-email", "123"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
          .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void register_withPasswordOver72Chars_returns400ValidationFailed () throws Exception {
        postJson("/api/players", creds("long@t.com", "x".repeat(73)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
          .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void register_withPasswordOver72BytesButUnder72Chars_returns400ValidationFailed () throws Exception {
        // 40 x "é" = 40 caracteres pero 80 bytes UTF-8: bcrypt lanzaría excepción (500) sin la validación
        postJson("/api/players", creds("bytes@t.com", "é".repeat(40)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
          // caracterización: la clave es el nombre de la propiedad del @AssertTrue, no "password"
          .andExpect(jsonPath("$.errors.passwordWithin72Bytes").exists());
    }

    @Test
    void register_withPasswordUnder8Chars_returns400ValidationFailed () throws Exception {
        postJson("/api/players", creds("short@t.com", "x".repeat(7)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
          .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void register_withExactly8CharPassword_returnsCreated () throws Exception {
        postJson("/api/players", creds("e" + UUID.randomUUID().toString().substring(0, 8) + "@t.com", "x".repeat(8)))
          .andExpect(status().isCreated());
    }

    @Test
    void register_newEmail_returnsCreated_andSameEmailAgainIs409EmailInUse () throws Exception {
        String body = creds("r" + UUID.randomUUID().toString().substring(0, 8) + "@t.com", "pw12345678");
        postJson("/api/players", body).andExpect(status().isCreated());
        postJson("/api/players", body)
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.code").value("EMAIL_IN_USE"));
    }

    @Test
    void patchMe_withValidNation_returnsProfileWithNation () throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        patchNation(token, "FIRE").andExpect(status().isOk()).andExpect(jsonPath("$.nation").value("FIRE"));
    }

    @Test
    void patchMe_withUnknownNation_returns400MalformedRequest () throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        patchNation(token, "PLUTO")
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void getMe_returnsOwnProfileWithEmailAndWinRate () throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        mvc.perform(get("/api/players/me").header("Authorization", token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email").exists())
          .andExpect(jsonPath("$.stats.win_rate").exists());
    }

    @Test
    void getMe_withoutAuthorizationHeader_returns401Unauthenticated () throws Exception {
        mvc.perform(get("/api/players/me"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void getMe_withTamperedSignature_returns401Unauthenticated () throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        // primer caracter de la firma: aporta 6 bits utiles (el ultimo solo 4, y cambiarlo puede dejar la firma intacta)
        int i = token.lastIndexOf('.') + 1;
        char c = token.charAt(i);
        String bad = token.substring(0, i) + (c == 'A' ? 'B' : 'A') + token.substring(i + 1);
        mvc.perform(get("/api/players/me").header("Authorization", bad))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void ranking_isArrayWithoutEmailAndWithoutRepeatedPlayers () throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        String body = mvc.perform(get("/api/players/ranking").header("Authorization", token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[*].player.email").doesNotExist())
          .andReturn().getResponse().getContentAsString();
        var ids = new HashSet<Long>();
        for (JsonNode e : new JsonMapper().readTree(body))
            assertTrue(ids.add(e.get("player").get("id").asLong()), "jugador repetido en el ranking");
    }
}
