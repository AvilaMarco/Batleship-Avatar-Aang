package com.codeoftheweb.salvo.dto.response;

import com.codeoftheweb.salvo.dto.GamePlayerDataDTO;
import com.codeoftheweb.salvo.enums.NationType;
import lombok.Data;

import java.util.List;

@Data
public class GameMatchDTO {
    private Long                    id;
    private NationType              nation;
    private String                  location;
    private List<GamePlayerDataDTO> gamePlayers;
}
