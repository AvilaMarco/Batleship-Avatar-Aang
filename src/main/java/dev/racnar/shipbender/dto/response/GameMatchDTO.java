package dev.racnar.shipbender.dto.response;

import dev.racnar.shipbender.dto.GamePlayerDataDTO;
import dev.racnar.shipbender.enums.NationType;
import lombok.Data;

import java.util.List;

@Data
public class GameMatchDTO {
    private Long                    id;
    private NationType              nation;
    private String                  location;
    private Integer                 turn;
    private List<GamePlayerDataDTO> gamePlayers;
}
