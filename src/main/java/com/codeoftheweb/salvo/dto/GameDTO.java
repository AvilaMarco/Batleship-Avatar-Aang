package com.codeoftheweb.salvo.dto;

import com.codeoftheweb.salvo.enums.NationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GameDTO {

    Long id;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    LocalDateTime created;

    @Enumerated(EnumType.STRING)
    NationType location;

    String direction;
    List<GamePlayerDTO> gamePlayers;
}
