package com.codeoftheweb.salvo.dto;

import com.codeoftheweb.salvo.enums.NationType;
import lombok.Data;

import java.util.List;

@Data
public class GameMapDTO {

    private Long id;
    private NationType nation;
    private String location;
    private List<GamePlayerDTO>  gamePlayers;
}
