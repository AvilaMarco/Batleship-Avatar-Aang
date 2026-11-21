package dev.racnar.shipbender.service.intereface;

import dev.racnar.shipbender.dto.response.GameCreatedDTO;
import dev.racnar.shipbender.dto.response.GameDataDTO;
import dev.racnar.shipbender.dto.response.GameMatchDTO;
import dev.racnar.shipbender.enums.NationType;
import org.springframework.security.core.Authentication;

public interface IMatchService {

    GameCreatedDTO createGame ( Authentication authentication, NationType nation, String location );

    GameCreatedDTO joinGame ( Authentication authentication, Long id );

    GameDataDTO statusGame ( Authentication authentication, Long gameId );

    GameMatchDTO getGame ( Authentication authentication, Long gameId );

}
