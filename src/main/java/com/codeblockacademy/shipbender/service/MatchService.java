package com.codeblockacademy.shipbender.service;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.response.GameCreatedDTO;
import com.codeblockacademy.shipbender.dto.response.GameDataDTO;
import com.codeblockacademy.shipbender.dto.response.GameMatchDTO;
import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.models.Game;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.service.intereface.IMatchService;
import com.codeblockacademy.shipbender.service.model.GamePlayerService;
import com.codeblockacademy.shipbender.service.model.GameService;
import com.codeblockacademy.shipbender.service.model.PlayerService;
import com.codeblockacademy.shipbender.service.validations.IGameValidation;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MatchService implements IMatchService {

    PlayerService     playerService;
    GameService       gameService;
    GamePlayerService gamePlayerService;

    IGameValidation gameValidation;

    public MatchService ( PlayerService playerService, GameService gameService, GamePlayerService gamePlayerService, IGameValidation gameValidation ) {
        this.playerService     = playerService;
        this.gameService       = gameService;
        this.gamePlayerService = gamePlayerService;
        this.gameValidation    = gameValidation;
    }

    @Override
    public GameCreatedDTO createGame ( Authentication authentication, NationType nation, String location ) {
        gameValidation.notExists(location);

        Player     player     = playerService.getPlayerAuthenticated(authentication);
        Game       game       = new Game(nation, location);
        GamePlayer gamePlayer = new GamePlayer(player, game);

        //playerService.save(player); // TODO: delete ?
        GameMapDTO gameDTO = gameService.save(game);
        gamePlayerService.save(gamePlayer);

        return new GameCreatedDTO(gameDTO.getId(), gamePlayer.getId());
    }

    @Override
    public GameCreatedDTO joinGame ( Authentication authentication, Long gameId ) {

        Player player = playerService.getPlayerAuthenticated(authentication);
        Game   game   = gameService.getGame(gameId);


        gameValidation.isNotFull(game);
//        ToDo: fix this
        /*gameService.gameNotContainsThePlayer(game, player.getId());*/

        GamePlayer gamePlayer = new GamePlayer(player, game);

        gamePlayerService.save(gamePlayer);

        return new GameCreatedDTO(game.getId(), gamePlayer.getId());
    }

    /* WEB SOCKET */
    public GameMatchDTO getGame ( Authentication authentication, Long gameId ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameValidation.containsThePlayer(gameId, player.getId());
        return gameService.getGameMatch(gameId);
    }


    public GameDataDTO statusGame ( Authentication authentication, Long gameId ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameValidation.containsThePlayer(gameId, player.getId());
        GameMatchDTO  data   = this.getGame(authentication, gameId);
        StatusGameDTO status = gameService.statusGame(gameId);
        return new GameDataDTO(data, status);
    }

}
