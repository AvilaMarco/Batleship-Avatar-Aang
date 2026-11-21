package dev.racnar.shipbender.player;

import dev.racnar.shipbender.game.GameService;

import dev.racnar.shipbender.game.dto.GameMapDTO;
import dev.racnar.shipbender.player.dto.PlayerStatsDTO;
import dev.racnar.shipbender.player.dto.MenuViewDTO;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvoService {

    PlayerService playerService;
    GameService   gameService;

    public SalvoService ( PlayerService playerService, GameService gameService ) {
        this.playerService = playerService;
        this.gameService   = gameService;
    }

    public MenuViewDTO getInfoGames ( Authentication authentication ) {
        PlayerStatsDTO       playerDTO    = playerService.getAnyPlayer(authentication);
        List<GameMapDTO>     games        = gameService.getGames();
        List<PlayerStatsDTO> playersScore = playerService.getPlayersScore();
        return new MenuViewDTO(playerDTO, games, playersScore);
    }
}
