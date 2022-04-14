package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.GamePlayerDTO;
import com.codeoftheweb.salvo.dto.response.GameCreatedDTO;
import com.codeoftheweb.salvo.enums.NationType;
import org.springframework.security.core.Authentication;

public interface IMatchService {

    GameCreatedDTO createGame(Authentication authentication, NationType nation, String location);

    GameCreatedDTO joinGame(Authentication authentication, Long id);

    /* WEB SOCKETS */

    GamePlayerDTO viewMatch(Authentication authentication, Long gameId);
}
