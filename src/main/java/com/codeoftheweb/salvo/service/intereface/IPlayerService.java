package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.PlayerScoreDTO;
import com.codeoftheweb.salvo.models.Player;
import com.codeoftheweb.salvo.service.PlayerService;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IPlayerService {

    PlayerDTO getPlayer(String email);
    PlayerDTO getPlayer(Authentication authentication);
    List<PlayerDTO> getPlayers();
    List<PlayerScoreDTO> getPlayersScore();
    PlayerDTO save(Player player);
    void validated(Player player);

}
