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
import com.codeblockacademy.shipbender.service.intereface.IGamePlayerService;
import com.codeblockacademy.shipbender.service.intereface.IGameService;
import com.codeblockacademy.shipbender.service.intereface.IMatchService;
import com.codeblockacademy.shipbender.service.intereface.IPlayerService;
import com.codeblockacademy.shipbender.service.validations.IGameValidation;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MatchService implements IMatchService {

    IPlayerService     playerService;
    IGameService       gameService;
    IGamePlayerService gamePlayerService;

    IGameValidation gameValidation;

    public MatchService ( IPlayerService playerService, IGameService gameService, IGamePlayerService gamePlayerService, IGameValidation gameValidation ) {
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
    @Override
    public GameMatchDTO getGame ( Authentication authentication, Long gameId ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameValidation.containsThePlayer(gameId, player.getId());
        GamePlayer gamePlayer = gamePlayerService.getGamePlayerBy(player.getId(), gameId);
        return gameService.getGameMatch(gameId, gamePlayer.getId());
    }


    @Override
    public GameDataDTO statusGame ( Authentication authentication, Long gameId ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameValidation.containsThePlayer(gameId, player.getId());
        GameMatchDTO  data   = this.getGame(authentication, gameId);
        StatusGameDTO status = gameService.statusGame(gameId);
        return new GameDataDTO(data, status);
    }

}
