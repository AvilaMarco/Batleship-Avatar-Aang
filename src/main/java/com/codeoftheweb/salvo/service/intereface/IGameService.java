package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.GameMapDTO;
import com.codeoftheweb.salvo.models.Game;

import java.util.List;

public interface IGameService {

    void gameNotExists(String direction);
    GameMapDTO getGame(Long id);
    List<GameMapDTO> getGames();
    GameMapDTO save(Game game);

    // Validations
    void gameIsNotFull(Game game);
    void gameNotContainsThePlayer(Game game, Long playerId);
}
