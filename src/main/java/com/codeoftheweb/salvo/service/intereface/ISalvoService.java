package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.InfoGamesDTO;
import org.springframework.security.core.Authentication;

public interface ISalvoService {

    InfoGamesDTO getInfoGames(Authentication authentication);
}
