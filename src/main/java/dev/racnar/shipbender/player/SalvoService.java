package dev.racnar.shipbender.player;

import dev.racnar.shipbender.game.GameService;

import dev.racnar.shipbender.game.dto.GameMapDTO;
import dev.racnar.shipbender.player.dto.PlayerDTO;
import dev.racnar.shipbender.player.dto.PlayerStatsDTO;
import dev.racnar.shipbender.player.dto.SignInPlayerDTO;
import dev.racnar.shipbender.player.dto.MenuViewDTO;
import dev.racnar.shipbender.shared.NationType;
import dev.racnar.shipbender.player.domain.Rol;
import dev.racnar.shipbender.player.domain.Player;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvoService {

    PlayerService  playerService;
    GameService    gameService;
    ModelMapper     mapper;
    PasswordEncoder encoder;

    public SalvoService ( PlayerService playerService, GameService gameService, ModelMapper mapper, PasswordEncoder encoder ) {
        this.playerService = playerService;
        this.gameService   = gameService;
        this.mapper        = mapper;
        this.encoder       = encoder;
    }

    public MenuViewDTO getInfoGames ( Authentication authentication ) {
        PlayerStatsDTO       playerDTO    = playerService.getAnyPlayer(authentication);
        List<GameMapDTO>     games        = gameService.getGames();
        List<PlayerStatsDTO> playersScore = playerService.getPlayersScore();
        return new MenuViewDTO(playerDTO, games, playersScore);
    }

    public PlayerDTO registerPlayer ( SignInPlayerDTO playerDTO ) {

        playerService.isNotRegister(playerDTO);

        Player player = mapper.map(playerDTO, Player.class);
        // TODO: Add mapper for encode password into model mapper
        String passwordEncode = encoder.encode(player.getPassword());
        player.setPassword(passwordEncode);
        player.addRol(Rol.PLAYER);
        return playerService.save(player);
    }

    public PlayerDTO setNationPlayer ( Authentication authentication, String nation ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        player.setNation(NationType.valueOf(nation));
        return playerService.save(player);
    }
}
