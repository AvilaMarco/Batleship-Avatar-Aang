package dev.racnar.shipbender.service.intereface;

import dev.racnar.shipbender.dto.PlayerDTO;
import dev.racnar.shipbender.dto.request.SignInPlayerDTO;
import dev.racnar.shipbender.dto.response.MenuViewDTO;
import org.springframework.security.core.Authentication;

public interface ISalvoService {

    MenuViewDTO getInfoGames ( Authentication authentication );

    PlayerDTO registerPlayer ( SignInPlayerDTO player );

    PlayerDTO setNationPlayer ( Authentication authentication, String nation );
}
