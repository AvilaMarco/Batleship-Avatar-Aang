package dev.racnar.shipbender.service;

import dev.racnar.shipbender.dto.GameMapDTO;
import dev.racnar.shipbender.dto.PlayerDTO;
import dev.racnar.shipbender.dto.PlayerStatsDTO;
import dev.racnar.shipbender.dto.request.SignInPlayerDTO;
import dev.racnar.shipbender.dto.response.MenuViewDTO;
import dev.racnar.shipbender.enums.NationType;
import dev.racnar.shipbender.enums.Rol;
import dev.racnar.shipbender.models.Player;
import dev.racnar.shipbender.service.intereface.IGameService;
import dev.racnar.shipbender.service.intereface.IPlayerService;
import dev.racnar.shipbender.service.intereface.ISalvoService;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvoService implements ISalvoService {

    IPlayerService  playerService;
    IGameService    gameService;
    ModelMapper     mapper;
    PasswordEncoder encoder;

    public SalvoService ( IPlayerService playerService, IGameService gameService, ModelMapper mapper, PasswordEncoder encoder ) {
        this.playerService = playerService;
        this.gameService   = gameService;
        this.mapper        = mapper;
        this.encoder       = encoder;
    }

    @Override
    public MenuViewDTO getInfoGames ( Authentication authentication ) {
        PlayerStatsDTO       playerDTO    = playerService.getAnyPlayer(authentication);
        List<GameMapDTO>     games        = gameService.getGames();
        List<PlayerStatsDTO> playersScore = playerService.getPlayersScore();
        return new MenuViewDTO(playerDTO, games, playersScore);
    }

    @Override
    public PlayerDTO registerPlayer ( SignInPlayerDTO playerDTO ) {

        playerService.isNotRegister(playerDTO);

        Player player = mapper.map(playerDTO, Player.class);
        // TODO: Add mapper for encode password into model mapper
        String passwordEncode = encoder.encode(player.getPassword());
        player.setPassword(passwordEncode);
        player.addRol(Rol.PLAYER);
        return playerService.save(player);
    }

    @Override
    public PlayerDTO setNationPlayer ( Authentication authentication, String nation ) {
        Player player = playerService.getPlayerAuthenticated(authentication);
        player.setNation(NationType.valueOf(nation));
        return playerService.save(player);
    }
}
