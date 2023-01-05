package com.codeblockacademy.shipbender.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static com.codeblockacademy.shipbender.config.ENV_VARIABLES.SECRET_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MatchControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    static String token;

    @BeforeAll
    static void setup () {
        LocalDateTime expired = LocalDateTime.now()
          .plusMinutes(1);
        Date expiredTime = Date.from(expired.atZone(ZoneId.systemDefault())
          .toInstant());

        token = "Bearer " + Jwts
          .builder()
          .setSubject("marco@admin.com")
          .claim("AUTHORITIES", Arrays.asList("ADMIN", "PLAYER", "GUEST"))
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(expiredTime)
          .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
          .compact();
    }

    @BeforeEach
    void a () {
        token = "";
    }

    @Test
    void aaa () throws JsonProcessingException {

        ObjectWriter writer = new ObjectMapper()
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
          .registerModule(new JavaTimeModule())
          .writer();

        LocalDate add = LocalDate.now();

        //LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String userJson = writer.writeValueAsString(add);
        System.out.println(userJson);
    }

    @Test
    void aaabbb () throws Exception {

        ResultMatcher statusOk = status()
          .isOk();

        var request = get("/api/match/{game_id}", 1).header("Authorization", token);
        mockMvc
          .perform(request)
          .andDo(print())
          .andExpectAll(
            statusOk
          );

        MvcResult response = mockMvc
          .perform(request)
          .andReturn();

        String content = response.getResponse()
          .getContentAsString();

    }
}
