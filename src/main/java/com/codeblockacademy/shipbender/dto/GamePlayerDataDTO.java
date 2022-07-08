package com.codeblockacademy.shipbender.dto;

import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GamePlayerDataDTO {
    private Long id;

    private String  emote;
    private Integer score;

    private PlayerDTO      player;
    private List<ShipDTO>  ships;
    private List<SalvoDTO> salvos; /*ToDo: delete*/

    /* Properties utils for gameplay */
    private Integer salvosAvailable;

    private List<String> salvosSuccessful;
    private List<String> salvosMissed;

}
