package com.codeoftheweb.salvo.service.main;

import com.codeoftheweb.salvo.dto.GameMapDTO;
import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.PlayerScoreDTO;
import com.codeoftheweb.salvo.dto.request.SignInPlayerDTO;
import com.codeoftheweb.salvo.dto.response.MenuViewDTO;
import com.codeoftheweb.salvo.enums.NationType;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.service.GamePlayerService;
import com.codeoftheweb.salvo.service.GameService;
import com.codeoftheweb.salvo.service.PlayerService;
import com.codeoftheweb.salvo.service.intereface.ISalvoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalvoService implements ISalvoService {

    @Autowired
    PlayerService playerService;

    @Autowired
    GameService gameService;

    @Autowired
    GamePlayerService gamePlayerService;

    @Autowired
    ModelMapper mapper;

    @Autowired
    PasswordEncoder encoder;

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
        return playerService.save(player);
    }

    @Override
    public PlayerDTO setNationPlayer ( Authentication authentication, String nation ) {
        Player player =  playerService.getPlayerAuthenticated(authentication);
        player.setNation(NationType.valueOf(nation));
        return playerService.save(player);
    }
}
