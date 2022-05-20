package com.codeblockacademy.shipbender.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

/*@SpringBootTest
@AutoConfigureMockMvc*/
public class demotest {

/*    @Autowired
    MockMvc mockMvc;*/

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
}
