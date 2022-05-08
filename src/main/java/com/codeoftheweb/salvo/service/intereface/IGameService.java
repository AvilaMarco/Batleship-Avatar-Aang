package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.GameMapDTO;
import com.codeoftheweb.salvo.dto.response.GameMatchDTO;
import com.codeoftheweb.salvo.models.Game;

import java.util.List;

public interface IGameService {

    GameMapDTO getGame ( Long id );

    GameMatchDTO getGameMatch ( Long id );

    List<GameMapDTO> getGames ();

    GameMapDTO save ( Game game );

    // Validations
    void gameNotExists ( String direction );

    void gameIsNotFull ( Game game );

    void gameNotContainsThePlayer ( Game game, Long playerId );

    void gameContainsThePlayer ( Long gameId, Long playerId );
}
