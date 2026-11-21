package dev.racnar.shipbender.game;

import dev.racnar.shipbender.player.PlayerService;

import dev.racnar.shipbender.game.dto.GameMapDTO;
import dev.racnar.shipbender.game.dto.GameCreatedDTO;
import dev.racnar.shipbender.game.dto.GameDataDTO;
import dev.racnar.shipbender.game.dto.GameMatchDTO;
import dev.racnar.shipbender.game.dto.StatusGameDTO;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.game.domain.Game;
import dev.racnar.shipbender.game.domain.GamePlayer;
import dev.racnar.shipbender.player.domain.Player;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

    PlayerService     playerService;
    GameService       gameService;
    GamePlayerService gamePlayerService;

    GameValidation gameValidation;

    public MatchService ( PlayerService playerService, GameService gameService, GamePlayerService gamePlayerService, GameValidation gameValidation ) {
        this.playerService     = playerService;
        this.gameService       = gameService;
        this.gamePlayerService = gamePlayerService;
        this.gameValidation    = gameValidation;
    }

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
        GamePlayer gamePlayer = gamePlayerService.getGamePlayerBy(player.getId(), gameId);
        return gameService.getGameMatch(gameId, gamePlayer.getId());
    }


    public GameDataDTO statusGame ( Authentication authentication, Long gameId ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        gameValidation.containsThePlayer(gameId, player.getId());
        GameMatchDTO  data   = this.getGame(authentication, gameId);
        StatusGameDTO status = gameService.statusGame(gameId);
        return new GameDataDTO(data, status);
    }

}
