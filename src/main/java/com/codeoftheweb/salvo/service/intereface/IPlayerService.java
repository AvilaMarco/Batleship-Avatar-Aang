package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.PlayerScoreDTO;
import com.codeoftheweb.salvo.dto.request.SignInPlayerDTO;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.service.PlayerService;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IPlayerService {

    Player getPlayer(String email);
    PlayerDTO getAnyPlayer(Authentication authentication);
    Player getPlayerAuthenticated(Authentication authentication);
    List<PlayerDTO> getPlayers();
    List<PlayerScoreDTO> getPlayersScore();
    PlayerDTO save(Player player);

    /* Validations */
    void validated(Player player);
    void isNotRegister(SignInPlayerDTO player);

}
