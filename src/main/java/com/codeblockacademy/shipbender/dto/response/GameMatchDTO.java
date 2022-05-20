package com.codeblockacademy.shipbender.dto.response;

import com.codeblockacademy.shipbender.dto.GamePlayerDataDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import lombok.Data;

import java.util.List;

@Data
public class GameMatchDTO {
    private Long                    id;
    private NationType              nation;
    private String                  location;
    private List<GamePlayerDataDTO> gamePlayers;
}
