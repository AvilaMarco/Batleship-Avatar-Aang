package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.dto.request.websocket.ShipDTO;
import com.codeblockacademy.shipbender.dto.response.StatusGameDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IGameplayService {
    StatusGameDTO createShips ( Authentication authentication, Long gameId, List<ShipDTO> shipsDTO );
}
