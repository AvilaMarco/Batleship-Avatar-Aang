package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.player.dto.PlayerDTO;

import lombok.Data;

@Data
public class GamePlayerDTO {

    Long id;

    PlayerDTO player;
    /*GameDTO   game;*/
    Integer   score;
}
