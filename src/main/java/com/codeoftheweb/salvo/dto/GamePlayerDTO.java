package com.codeoftheweb.salvo.dto;

import lombok.Data;

@Data
public class GamePlayerDTO {

    Long id;

    PlayerDTO player;
    Integer score;
}
