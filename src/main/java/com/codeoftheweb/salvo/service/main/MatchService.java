package com.codeoftheweb.salvo.service.main;

import com.codeoftheweb.salvo.dto.GameMapDTO;
import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.dto.request.websocket.ShipDTO;
import com.codeoftheweb.salvo.dto.response.GameCreatedDTO;
import com.codeoftheweb.salvo.dto.response.GameMatchDTO;
import com.codeoftheweb.salvo.dto.response.StatusGameDTO;
import com.codeoftheweb.salvo.enums.NationType;
import com.codeoftheweb.salvo.models.Game;
import com.codeoftheweb.salvo.models.GamePlayer;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.models.Ship;
import com.codeoftheweb.salvo.service.GamePlayerService;
import com.codeoftheweb.salvo.service.GameService;
import com.codeoftheweb.salvo.service.PlayerService;
import com.codeoftheweb.salvo.service.intereface.IMatchService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

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
    public GameCreatedDTO createGame ( Authentication authentication, NationType nation, String location ) {
        gameService.gameNotExists(location);

        Player     player     = playerService.getPlayerAuthenticated(authentication);
        Game       game       = new Game(nation, location);
        GamePlayer gamePlayer = new GamePlayer(player, game);

        //playerService.save(player); // TODO: delete ?
        GameMapDTO    gameDTO       = gameService.save(game);
        GamePlayerDTO gamePlayerDTO = gamePlayerService.save(gamePlayer);

        return new GameCreatedDTO(gameDTO.getId(), gamePlayerDTO.getId());
    }

    @Override
    public GameCreatedDTO joinGame ( Authentication authentication, Long gameId ) {

        Player     player  = playerService.getPlayerAuthenticated(authentication);
        GameMapDTO gameDTO = gameService.getGame(gameId);
        Game       game    = mapper.map(gameDTO, Game.class);

        gameService.gameIsNotFull(game);
        gameService.gameNotContainsThePlayer(game, player.getId());

        GamePlayer gamePlayer = new GamePlayer(player, game);

        gamePlayerService.save(gamePlayer);

        return new GameCreatedDTO(game.getId(), gamePlayer.getId());
    }

    /* WEB SOCKET */
    public GameMatchDTO getGame ( Authentication authentication, Long gameId ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameService.gameContainsThePlayer(gameId, player.getId());
        return gameService.getGameMatch(gameId);
    }


    public StatusGameDTO statusGame ( Authentication authentication, Long gameId ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameService.gameContainsThePlayer(gameId, player.getId());
        return gameService.statusGame(gameId);
    }

    @Override
    public StatusGameDTO createShips ( Authentication authentication, Long gameId, List<ShipDTO> shipsDTO ) {
        Player        player        = playerService.getPlayerAuthenticated(authentication);
        GamePlayerDTO gamePlayerDTO = gamePlayerService.getGamePlayerBy(player.getId(), gameId);
        GamePlayer    gamePlayer    = mapper.map(gamePlayerDTO, GamePlayer.class);
        // ToDo: create exception for when gamePlayer has ships
        if (gamePlayer.hasShips()) throw new RuntimeException();
        List<Ship> ships = shipsDTO.stream()
          .map(s -> mapper.map(s, Ship.class))
          .collect(toList());
        gamePlayer.setShips(ships);
        gamePlayerService.save(gamePlayer);
        return gameService.statusGame(gameId);
    }
}
