package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.dto.PlayerDTO;
import com.codeblockacademy.shipbender.dto.PlayerStatsDTO;
import com.codeblockacademy.shipbender.dto.request.SignInPlayerDTO;
import com.codeblockacademy.shipbender.models.Player;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IPlayerService {

    Player getPlayer ( String email );

    PlayerStatsDTO getAnyPlayer ( Authentication authentication );

    Player getPlayerAuthenticated ( Authentication authentication );

    List<PlayerDTO> getPlayers ();

    List<PlayerStatsDTO> getPlayersScore ();

    PlayerDTO save ( Player player );

    /* Validations */
    void validated ( Player player );

    void isNotRegister ( SignInPlayerDTO player );

}
