package dev.racnar.shipbender.dto.response;

import dev.racnar.shipbender.enums.GameStatus;
import dev.racnar.shipbender.enums.PlayerStatus;
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
