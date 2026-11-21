package dev.racnar.shipbender.service.intereface;

import dev.racnar.shipbender.dto.request.websocket.ShipDTO;
import dev.racnar.shipbender.dto.response.GameDataDTO;
import dev.racnar.shipbender.dto.response.UserEmoteDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IGameplayService {
    GameDataDTO createShips ( Authentication authentication, Long gameId, List<ShipDTO> shipsDTO );

    UserEmoteDTO sendEmote ( Long gameId, String emote );
}
