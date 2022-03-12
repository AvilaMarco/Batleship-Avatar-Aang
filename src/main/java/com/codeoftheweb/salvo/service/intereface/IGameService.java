package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.GameDTO;
import com.codeoftheweb.salvo.models.Game;

import java.util.List;

public interface IGameService {

    void gameNotExists(String direction);
    GameDTO getGame(Long id);
    List<GameDTO> getGames();
    GameDTO save(Game game);

    // Validations
    void gameIsNotFull(Game game);
    void gameNotContainsThePlayer(Game game, Long playerId);
}
