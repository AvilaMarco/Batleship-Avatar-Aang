package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.GameDTO;
import com.codeoftheweb.salvo.models.Game;

import java.util.List;

public interface IGameService {

    GameDTO getGame(String direction);
    GameDTO getGame(Long id);
    List<GameDTO> getGames();
    GameDTO save(Game game);
}
