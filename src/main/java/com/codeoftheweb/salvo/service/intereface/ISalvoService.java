package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.InfoGamesDTO;
import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.request.SignInPlayerDTO;
import com.codeoftheweb.salvo.models.Player;
import org.springframework.security.core.Authentication;

public interface ISalvoService {

    InfoGamesDTO getInfoGames(Authentication authentication);

    PlayerDTO registerPlayer(SignInPlayerDTO player);
}
