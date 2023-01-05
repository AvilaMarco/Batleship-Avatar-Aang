package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.dto.response.GameCreatedDTO;
import com.codeblockacademy.shipbender.dto.response.GameDataDTO;
import com.codeblockacademy.shipbender.dto.response.GameMatchDTO;
import com.codeblockacademy.shipbender.enums.NationType;
import org.springframework.security.core.Authentication;

public interface IMatchService {

    GameCreatedDTO createGame ( Authentication authentication, NationType nation, String location );

    GameCreatedDTO joinGame ( Authentication authentication, Long id );

    GameDataDTO statusGame ( Authentication authentication, Long gameId );

    GameMatchDTO getGame ( Authentication authentication, Long gameId );

}
