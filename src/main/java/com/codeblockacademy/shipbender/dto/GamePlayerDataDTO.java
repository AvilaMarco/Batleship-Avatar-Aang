package com.codeblockacademy.shipbender.dto;

import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import lombok.Data;

import java.util.List;

@Data
public class GamePlayerDataDTO {
    private Long id;

    private String  emote;
    private Integer score;

    private PlayerDTO      player;
    private List<ShipDTO>  ships;
    private List<SalvoDTO> salvos;

}
