package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.ShipDTO;
import com.codeoftheweb.salvo.dto.response.GameCreatedDTO;
import com.codeoftheweb.salvo.dto.response.GameMatchDTO;
import com.codeoftheweb.salvo.dto.response.StatusGameDTO;
import com.codeoftheweb.salvo.enums.NationType;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface IMatchService {

    GameCreatedDTO createGame ( Authentication authentication, NationType nation, String location );

    GameCreatedDTO joinGame ( Authentication authentication, Long id );

    StatusGameDTO statusGame ( Authentication authentication, Long gameId );

    GameMatchDTO getGame ( Authentication authentication, Long gameId );

    /* WEB SOCKETS */


    StatusGameDTO createShips ( Authentication authentication, Long gameId, List<ShipDTO> ships );
}
