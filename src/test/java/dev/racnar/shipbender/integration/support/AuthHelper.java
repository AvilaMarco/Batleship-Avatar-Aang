package dev.racnar.shipbender.integration.support;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class AuthHelper {
    private static final ObjectMapper JSON = new JsonMapper();

    private AuthHelper () {}

    /** Devuelve el token ya con prefijo "Bearer ". */
    public static String loginToken ( MockMvc mvc, String email, String password ) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(JSON.writeValueAsString(new Creds(email, password))))
          .andExpect(status().isOk())
          .andReturn().getResponse().getContentAsString();
        JsonNode node = JSON.readTree(body);
        return node.get("token").asString();
    }

    public static String registerAndLogin ( MockMvc mvc ) throws Exception {
        // el email admite max 30 caracteres
        String email = "u" + UUID.randomUUID().toString().substring(0, 8) + "@t.com";
        mvc.perform(post("/api/players").contentType(MediaType.APPLICATION_JSON)
            .content(JSON.writeValueAsString(new Creds(email, "pw12345678", "Tester"))))
          .andExpect(status().isCreated());
        return loginToken(mvc, email, "pw12345678");
    }

    /** Crea una partida FIRE con location unica y devuelve su id (POST /api/games). */
    public static long createGame ( MockMvc mvc, String token ) throws Exception {
        String body = "{\"nation\":\"FIRE\",\"location\":\"T" + UUID.randomUUID().toString().substring(0, 8) + "\"}";
        String res = mvc.perform(post("/api/games").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isCreated())
          .andReturn().getResponse().getContentAsString();
        return JSON.readTree(res).get("id").asLong();
    }

    /** El jugador del token se une a la partida (POST /api/games/{id}/players). */
    public static void join ( MockMvc mvc, String token, long id ) throws Exception {
        mvc.perform(post("/api/games/{id}/players", id).header("Authorization", token))
          .andExpect(status().isCreated());
    }

    public record Creds(String email, String password, String name) {
        Creds ( String email, String password ) { this(email, password, null); }
    }
}
