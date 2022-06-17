package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.response.GameMatchDTO;
import com.codeblockacademy.shipbender.models.Game;

import java.util.List;

public interface IGameService {

    Game getGame ( Long id );

    GameMatchDTO getGameMatch ( Long id );

    List<GameMapDTO> getGames ();

    GameMapDTO save ( Game game );

}
