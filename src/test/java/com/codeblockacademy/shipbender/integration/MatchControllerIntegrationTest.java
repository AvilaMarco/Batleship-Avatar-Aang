package com.codeblockacademy.shipbender.integration;

import com.codeblockacademy.shipbender.integration.support.AuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MatchControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    String token;

    @BeforeEach
    void setup () throws Exception {
        token = AuthHelper.loginToken(mockMvc, "marco@admin.com", "123");
    }

    @Test
    void getMatch_withAdminToken_returnsOk () throws Exception {
        mockMvc.perform(get("/api/match/{game_id}", 1).header("Authorization", token))
          .andExpect(status().isOk());
    }
}
