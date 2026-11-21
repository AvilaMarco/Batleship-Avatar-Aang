package dev.racnar.shipbender.player.dto;

import dev.racnar.shipbender.game.dto.GameMapDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuViewDTO {

    PlayerDTO            player;
    List<GameMapDTO>     games;
    List<PlayerStatsDTO> players;

}
