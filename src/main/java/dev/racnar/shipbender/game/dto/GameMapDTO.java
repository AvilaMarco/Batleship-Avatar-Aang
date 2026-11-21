package dev.racnar.shipbender.game.dto;

import dev.racnar.shipbender.shared.NationType;
import lombok.Data;

import java.util.List;

@Data
public class GameMapDTO {

    private Long                id;
    private NationType          nation;
    private String              location;
    private List<GamePlayerDTO> gamePlayers;
}
