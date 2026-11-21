package dev.racnar.shipbender.service;

import dev.racnar.shipbender.dto.GameMapDTO;
import dev.racnar.shipbender.dto.response.GameCreatedDTO;
import dev.racnar.shipbender.dto.response.GameDataDTO;
import dev.racnar.shipbender.dto.response.GameMatchDTO;
import dev.racnar.shipbender.dto.response.StatusGameDTO;
import dev.racnar.shipbender.enums.NationType;
import dev.racnar.shipbender.models.Game;
import dev.racnar.shipbender.models.GamePlayer;
import dev.racnar.shipbender.models.Player;
import dev.racnar.shipbender.service.intereface.IGamePlayerService;
import dev.racnar.shipbender.service.intereface.IGameService;
import dev.racnar.shipbender.service.intereface.IMatchService;
import dev.racnar.shipbender.service.intereface.IPlayerService;
import dev.racnar.shipbender.service.validations.IGameValidation;
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
