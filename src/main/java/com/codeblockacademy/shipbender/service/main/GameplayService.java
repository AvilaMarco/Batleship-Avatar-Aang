package com.codeblockacademy.shipbender.service.main;

import com.codeblockacademy.shipbender.dto.GamePlayerDTO;
import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.models.GamePlayer;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.models.Ship;
import com.codeblockacademy.shipbender.service.GamePlayerService;
import com.codeblockacademy.shipbender.service.GameService;
import com.codeblockacademy.shipbender.service.PlayerService;
import com.codeblockacademy.shipbender.service.intereface.IGameplayService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class GameplayService implements IGameplayService {

    PlayerService     playerService;
    GameService       gameService;
    GamePlayerService gamePlayerService;
    ModelMapper       mapper;

    public GameplayService ( PlayerService playerService, GameService gameService, GamePlayerService gamePlayerService, ModelMapper mapper ) {
        this.playerService     = playerService;
        this.gameService       = gameService;
        this.gamePlayerService = gamePlayerService;
        this.mapper            = mapper;
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
        //Todo: arreglar problema al mapear cosas que no existen, en este caso el id
        gamePlayer.getGame()
          .setId(gameId);
        gamePlayerService.save(gamePlayer);
        return gameService.statusGame(gameId);
    }
}
