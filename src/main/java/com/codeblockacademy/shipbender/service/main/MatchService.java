package com.codeblockacademy.shipbender.service.main;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.GamePlayerDTO;
import com.codeblockacademy.shipbender.dto.response.GameCreatedDTO;
import com.codeblockacademy.shipbender.dto.response.GameMatchDTO;
import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.models.Game;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.service.GamePlayerService;
import com.codeblockacademy.shipbender.service.GameService;
import com.codeblockacademy.shipbender.service.PlayerService;
import com.codeblockacademy.shipbender.service.intereface.IMatchService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MatchService implements IMatchService {

    PlayerService     playerService;
    GameService       gameService;
    GamePlayerService gamePlayerService;
    ModelMapper       mapper;

    public MatchService ( PlayerService playerService, GameService gameService, GamePlayerService gamePlayerService, ModelMapper mapper ) {
        this.playerService     = playerService;
        this.gameService       = gameService;
        this.gamePlayerService = gamePlayerService;
        this.mapper            = mapper;
    }

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


}
