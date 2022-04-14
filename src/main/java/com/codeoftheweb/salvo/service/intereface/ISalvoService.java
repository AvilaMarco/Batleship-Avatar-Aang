package com.codeoftheweb.salvo.service.intereface;

import com.codeoftheweb.salvo.dto.PlayerDTO;
import com.codeoftheweb.salvo.dto.request.SignInPlayerDTO;
import com.codeoftheweb.salvo.dto.response.MenuViewDTO;
import org.springframework.security.core.Authentication;

public interface ISalvoService {

    MenuViewDTO getInfoGames(Authentication authentication);

    PlayerDTO registerPlayer(SignInPlayerDTO player);
}
