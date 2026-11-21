package dev.racnar.shipbender.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDataDTO {
    GameMatchDTO  data;
    StatusGameDTO status;
}
