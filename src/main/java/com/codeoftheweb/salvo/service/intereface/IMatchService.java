package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.GameDTO;
import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.dto.response.GameCreatedDTO;
import org.springframework.security.core.Authentication;

public interface IMatchService {

    GameCreatedDTO createGame(Authentication authentication, String location, String direction);

    GameCreatedDTO joinGame(Authentication authentication, Long id);

    /* WEB SOCKETS */

    GamePlayerDTO viewMatch(Authentication authentication, Long gameId);
}
