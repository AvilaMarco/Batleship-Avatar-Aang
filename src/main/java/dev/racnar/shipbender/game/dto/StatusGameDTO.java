package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.game.domain.GameStatus;
import dev.racnar.shipbender.game.domain.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatusGameDTO {
    GameStatus   game;
    PlayerStatus host;
    PlayerStatus client;
}
