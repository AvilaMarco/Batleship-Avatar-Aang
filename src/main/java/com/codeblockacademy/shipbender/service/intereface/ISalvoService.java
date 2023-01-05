package com.codeblockacademy.shipbender.service.intereface;

import com.codeblockacademy.shipbender.dto.PlayerDTO;
import com.codeblockacademy.shipbender.dto.request.SignInPlayerDTO;
import com.codeblockacademy.shipbender.dto.response.MenuViewDTO;
import org.springframework.security.core.Authentication;

public interface ISalvoService {

    MenuViewDTO getInfoGames ( Authentication authentication );

    PlayerDTO registerPlayer ( SignInPlayerDTO player );

    PlayerDTO setNationPlayer ( Authentication authentication, String nation );
}
