package dev.racnar.shipbender.service.intereface;

import dev.racnar.shipbender.dto.PlayerDTO;
import dev.racnar.shipbender.dto.PlayerStatsDTO;
import dev.racnar.shipbender.dto.request.SignInPlayerDTO;
import dev.racnar.shipbender.models.Player;
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
