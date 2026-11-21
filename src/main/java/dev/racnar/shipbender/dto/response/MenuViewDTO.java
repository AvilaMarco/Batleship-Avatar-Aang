package dev.racnar.shipbender.dto.response;

import dev.racnar.shipbender.dto.GameMapDTO;
import dev.racnar.shipbender.dto.PlayerDTO;
import dev.racnar.shipbender.dto.PlayerStatsDTO;
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
