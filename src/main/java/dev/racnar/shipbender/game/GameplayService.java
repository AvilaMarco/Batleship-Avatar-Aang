package dev.racnar.shipbender.game;

import dev.racnar.shipbender.player.PlayerService;

import dev.racnar.shipbender.game.dto.ShipDTO;
import dev.racnar.shipbender.game.dto.GameDataDTO;
import dev.racnar.shipbender.game.dto.UserEmoteDTO;
import dev.racnar.shipbender.game.domain.GamePlayer;
import dev.racnar.shipbender.player.domain.Player;
import dev.racnar.shipbender.game.domain.Ship;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameplayService {

    PlayerService     playerService;
    GameService       gameService;
    GamePlayerService gamePlayerService;
    MatchService      matchService;
    ModelMapper        mapper;

    public GameplayService ( PlayerService playerService, GameService gameService, GamePlayerService gamePlayerService, MatchService matchService, ModelMapper mapper ) {
        this.playerService     = playerService;
        this.gameService       = gameService;
        this.gamePlayerService = gamePlayerService;
        this.matchService      = matchService;
        this.mapper            = mapper;
    }

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
