package dev.racnar.shipbender.service;

import dev.racnar.shipbender.dto.request.websocket.ShipDTO;
import dev.racnar.shipbender.dto.response.GameDataDTO;
import dev.racnar.shipbender.dto.response.UserEmoteDTO;
import dev.racnar.shipbender.models.GamePlayer;
import dev.racnar.shipbender.models.Player;
import dev.racnar.shipbender.models.Ship;
import dev.racnar.shipbender.service.intereface.*;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameplayService implements IGameplayService {

    IPlayerService     playerService;
    IGameService       gameService;
    IGamePlayerService gamePlayerService;
    IMatchService      matchService;
    ModelMapper        mapper;

    public GameplayService ( IPlayerService playerService, IGameService gameService, IGamePlayerService gamePlayerService, IMatchService matchService, ModelMapper mapper ) {
        this.playerService     = playerService;
        this.gameService       = gameService;
        this.gamePlayerService = gamePlayerService;
        this.matchService      = matchService;
        this.mapper            = mapper;
    }

    @Override
    @Transactional
    public GameDataDTO createShips ( Authentication authentication, Long gameId, List<ShipDTO> shipsDTO ) {
        Player     player     = playerService.getPlayerAuthenticated(authentication);
        GamePlayer gamePlayer = gamePlayerService.getGamePlayerBy(player.getId(), gameId);
        // ToDo: create exception for when gamePlayer has ships
        if (gamePlayer.hasShips()) throw new RuntimeException();
        Set<Ship> ships = shipsDTO.stream()
          .map(s -> mapper.map(s, Ship.class))
          .collect(Collectors.toSet());
        gamePlayer.setShips(ships);
        gamePlayerService.save(gamePlayer);
        return matchService.statusGame(authentication, gameId);
    }

    @Override
    public UserEmoteDTO sendEmote ( Long gameId, String emote ) {
        Authentication auth = SecurityContextHolder.getContext()
          .getAuthentication();
        Player     player     = playerService.getPlayerAuthenticated(auth);
        GamePlayer gamePlayer = gamePlayerService.getGamePlayerBy(player.getId(), gameId);
        gamePlayer.setEmote(emote);
        gamePlayerService.save(gamePlayer);
        return new UserEmoteDTO(gamePlayer.getId(), emote);
    }
}
