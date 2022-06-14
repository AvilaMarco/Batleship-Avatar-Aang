package com.codeblockacademy.shipbender.service.main;

import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import com.codeblockacademy.shipbender.enums.GameStatus;
import com.codeblockacademy.shipbender.enums.PlayerStatus;
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
import java.util.Set;
import java.util.stream.Collectors;

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
        Player     player     = playerService.getPlayerAuthenticated(authentication);
        GamePlayer gamePlayer = gamePlayerService.getGamePlayerBy(player.getId(), gameId);
        // ToDo: create exception for when gamePlayer has ships
        if (gamePlayer.hasShips()) throw new RuntimeException();
        Set<Ship> ships = shipsDTO.stream()
          .map(s -> mapper.map(s, Ship.class))
          .collect(Collectors.toSet());
        gamePlayer.setShips(ships);
        gamePlayerService.save(gamePlayer);
        gameService.save(gamePlayer.getGame());
//        return gameService.statusGame(gameId);
        return new StatusGameDTO(GameStatus.IN_GAME, PlayerStatus.HOST_WITH_SHIPS, PlayerStatus.CLIENT_WAITING);
    }
}
