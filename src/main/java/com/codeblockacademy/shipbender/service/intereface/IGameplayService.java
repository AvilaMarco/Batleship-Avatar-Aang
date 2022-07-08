package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import com.codeblockacademy.shipbender.dto.response.GameDataDTO;
import com.codeblockacademy.shipbender.dto.response.UserEmoteDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IGameplayService {
    GameDataDTO createShips ( Authentication authentication, Long gameId, List<ShipDTO> shipsDTO );

    UserEmoteDTO sendEmote ( Long gameId, String emote );
}
