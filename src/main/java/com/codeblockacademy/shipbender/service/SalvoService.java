package com.codeblockacademy.shipbender.service;

import com.codeblockacademy.shipbender.dto.GameMapDTO;
import com.codeblockacademy.shipbender.dto.PlayerDTO;
import com.codeblockacademy.shipbender.dto.PlayerScoreDTO;
import com.codeblockacademy.shipbender.dto.request.SignInPlayerDTO;
import com.codeblockacademy.shipbender.dto.response.MenuViewDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import com.codeblockacademy.shipbender.enums.Rol;
import com.codeblockacademy.shipbender.models.Player;
import com.codeblockacademy.shipbender.service.intereface.IGameService;
import com.codeblockacademy.shipbender.service.intereface.IPlayerService;
import com.codeblockacademy.shipbender.service.intereface.ISalvoService;
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
        PlayerScoreDTO       playerDTO    = playerService.getAnyPlayer(authentication);
        List<GameMapDTO>     games        = gameService.getGames();
        List<PlayerScoreDTO> playersScore = playerService.getPlayersScore();
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
