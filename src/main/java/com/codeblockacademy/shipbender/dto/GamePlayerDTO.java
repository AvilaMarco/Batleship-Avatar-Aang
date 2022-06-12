package com.codeblockacademy.shipbender.dto;

import lombok.Data;

@Data
public class GamePlayerDTO {

    Long id;

    PlayerDTO player;
    /*GameDTO   game;*/
    Integer   score;
}
