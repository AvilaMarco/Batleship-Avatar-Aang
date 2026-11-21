package com.codeblockacademy.shipbender.integration;

import com.codeblockacademy.shipbender.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;
import com.codeblockacademy.shipbender.integration.support.AuthHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void login_withSeedUser_returnsEmailAndBearerToken () throws Exception {
        postJson("/api/players/login", creds("marco@admin.com", "123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email").value("marco@admin.com"))
          .andExpect(jsonPath("$.token", startsWith("Bearer ")));
    }

    @Test
    void login_withWrongPassword_returnsNotFound () throws Exception {
        postJson("/api/players/login", creds("marco@admin.com", "wrong")).andExpect(status().isNotFound());
    }

    @Test
    void login_withMalformedEmail_returnsBadRequestWithEmailFieldError () throws Exception {
        postJson("/api/players/login", creds("not-an-email", "123"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.error_fields.email").exists());
    }

    @Test
    void register_newEmail_returnsCreated_andSameEmailAgainConflicts () throws Exception {
        String body = creds("r" + UUID.randomUUID().toString().substring(0, 8) + "@t.com", "pw123");
        postJson("/api/players", body).andExpect(status().isCreated());
        postJson("/api/players", body).andExpect(status().isConflict());
    }

    @Test
    void setNation_withRegisteredUserToken_returnsOk () throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        mvc.perform(post("/api/players/nation/FIRE").header("Authorization", token)).andExpect(status().isOk());
    }

    @Test
    void setNation_withoutAuthorizationHeader_isRejected () throws Exception {
        mvc.perform(post("/api/players/nation/FIRE")).andExpect(status().isForbidden());
    }

    @Test
    void setNation_withTamperedSignature_throwsInsteadOfResponding () throws Exception {
        String token = AuthHelper.registerAndLogin(mvc);
        // primer caracter de la firma: aporta 6 bits utiles (el ultimo solo 4, y cambiarlo puede dejar la firma intacta)
        int i = token.lastIndexOf('.') + 1;
        char c = token.charAt(i);
        String bad = token.substring(0, i) + (c == 'A' ? 'B' : 'A') + token.substring(i + 1);
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
          () -> mvc.perform(post("/api/players/nation/FIRE").header("Authorization", bad)));
    }
}
