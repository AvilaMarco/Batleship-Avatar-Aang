package com.codeoftheweb.salvo.service.main;

import com.codeoftheweb.salvo.dto.GameDTO;
import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.response.GameCreatedDTO;
import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.service.GamePlayerService;
import com.codeoftheweb.salvo.service.GameService;
import com.codeoftheweb.salvo.service.PlayerService;
import com.codeoftheweb.salvo.service.intereface.IMatchService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MatchService implements IMatchService {

    @Autowired
    PlayerService playerService;

    @Autowired
    GameService gameService;

    @Autowired
    GamePlayerService gamePlayerService;

    @Autowired
    ModelMapper mapper;

    @Override
    public GameCreatedDTO createGame(Authentication authentication, String location, String direction){
        gameService.gameNotExists(direction);

        Player player = playerService.getPlayerAuthenticated(authentication);
        Game game = new Game();
        GamePlayer gamePlayer = new GamePlayer(player, game);

        playerService.save(player); // TODO: delete ?
        GameDTO gameDTO = gameService.save(game);
        GamePlayerDTO gamePlayerDTO = gamePlayerService.save(gamePlayer);

        return new GameCreatedDTO(gameDTO.getId(), gamePlayerDTO.getId());
    }

    @Override
    public GameCreatedDTO joinGame(Authentication authentication, Long GameId) {

        Player player = playerService.getPlayerAuthenticated(authentication);
        GameDTO gameDTO =  gameService.getGame(GameId);
        Game game = mapper.map(gameDTO, Game.class);

        gameService.gameIsNotFull(game);
        gameService.gameNotContainsThePlayer(game, player.getId());

        GamePlayer gamePlayer = new GamePlayer(player, game);

        gameService.save(game); // TODO: delete ?
        gamePlayerService.save(gamePlayer);

        return new GameCreatedDTO(game.getId(), gamePlayer.getId());
    }

    /* WEB SOCKET */
    public GamePlayerDTO viewMatch(Authentication authentication, Long gameId){
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameService.gameExists(gameId);

        return gamePlayerService.getGamePlayerBy(player.getId(), gameId);
    }
}
