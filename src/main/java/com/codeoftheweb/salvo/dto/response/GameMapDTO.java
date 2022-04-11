package com.codeoftheweb.salvo.dto.response;

import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.enums.NationType;
import lombok.Data;

import java.util.List;

@Data
public class GameMapDTO {

    private Integer id;
    private NationType nation;
    private String location;
    private List<GamePlayerDTO>  gamePlayers;
}
