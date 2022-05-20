package com.codeblockacademy.shipbender.dto;

import com.codeblockacademy.shipbender.enums.NationType;
import lombok.Data;

import java.util.List;

@Data
public class GameMapDTO {

    private Long                id;
    private NationType          nation;
    private String              location;
    private List<GamePlayerDTO> gamePlayers;
}
