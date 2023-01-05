package com.codeblockacademy.shipbender.dto.response;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.PlayerDTO;
import com.codeblockacademy.shipbender.dto.PlayerStatsDTO;
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
